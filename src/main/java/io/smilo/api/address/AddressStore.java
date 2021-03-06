/*
 * Copyright (c) 2018 Smilo Platform B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.smilo.api.address;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smilo.commons.db.Store;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static java.nio.ByteBuffer.allocateDirect;

@Component
public class AddressStore {

    private static final Logger LOGGER = Logger.getLogger(AddressStore.class);

    private static final String COLLECTION_NAME = "address";
    private static final String UTF_8 = "UTF-8";
    private final Store store;
    private final ObjectMapper dataMapper;

    public AddressStore(Store store, ObjectMapper dataMapper) {
        this.store = store;
        this.dataMapper = dataMapper;
        store.initializeCollection(COLLECTION_NAME);
    }

    /*
     * Looks up an account in the ledger, if account exists return it otherwise create
     * new account with 0 balance.
     */
    public Address findOrCreate(String findAddress) {
        AddressDTO result = getByAddress(findAddress);

        if (result == null) {
            Map<String, BigInteger> balances = new HashMap<>();
            balances.put("000x00123", BigInteger.ZERO);
            Address address = new Address(findAddress, balances, -1);
            writeToFile(address);
            return address;
        }

        return AddressDTO.toAddress(result);
    }

    /**
     * Writes Address to file.
     *
     * @param address account to save
     */
    public void writeToFile(Address address) {
        try {
            AddressDTO dto = AddressDTO.toDTO(address);
            store.put(COLLECTION_NAME, address.getAddress().getBytes(UTF_8), dataMapper.writeValueAsBytes(dto));
        } catch (Exception e) {
            LOGGER.error("Unable to convert address to byte array " + e);
        }
    }

    public AddressDTO getByAddress(String address) {
        if (address == null) {
            return null;
        }
        byte[] raw;
        try {
            raw = store.get(COLLECTION_NAME, address.getBytes(UTF_8));
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Unsupported Encoding error.", e);
            return null;
        }
        if (raw == null){
            return null;
        }
        AddressDTO result = null;
        try {
            result = dataMapper.readValue(raw, AddressDTO.class);
        } catch (IOException e) {
            LOGGER.debug("getByAddress, Unable to convert byte[] to AddressDTO ", e);
        }

        return result;
    }
}
