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

package io.smilo.api.block.data.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smilo.api.db.Store;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;

@Component
public class TransactionStore {
    private static final Logger LOGGER = Logger.getLogger(TransactionStore.class);
    private static final String COLLECTION_NAME = "transaction";

    private final Store store;
    private final ObjectMapper dataMapper;

    public TransactionStore(Store store, ObjectMapper dataMapper) {
        this.store = store;
        this.dataMapper = dataMapper;

        store.initializeCollection(COLLECTION_NAME);
    }

    /**
     * Writes the given Transaction to LMDB.
     * @param transaction
     */
    public void writeTransactionToFile(Transaction transaction) {
        final ByteBuffer keyBuffer;
        final ByteBuffer valueBuffer;

        // Try and convert the transaction to a byte key buffer and value key buffer.
        try {
            TransactionDTO dto = TransactionDTO.toDTO(transaction);
            byte[] dataHashBytes = dto.getDataHash().getBytes();
            byte[] valueBytes = dataMapper.writeValueAsBytes(dto);

            keyBuffer = ByteBuffer.allocateDirect(dataHashBytes.length);
            valueBuffer = ByteBuffer.allocateDirect(valueBytes.length);

            keyBuffer.put(dataHashBytes).flip();
            valueBuffer.put(valueBytes).flip();
        }
        catch(Exception ex) {
            LOGGER.error("Unable to convert transaction to byte array " + ex);
            return;
        }

        // Write to LMDB
        store.put(COLLECTION_NAME, keyBuffer, valueBuffer);
    }

    /**
     * Reads the Transaction with the given id from LMDB.
     * @param id The id of the Transaction. This is equal to the data hash.
     * @return
     */
    public Transaction getTransaction(String id) {
        byte[] idBytes = id.getBytes();
        ByteBuffer keyBuffer = ByteBuffer.allocateDirect(idBytes.length);
        keyBuffer.put(idBytes).flip();

        byte[] rawTransaction = store.get(COLLECTION_NAME, keyBuffer);
        TransactionDTO dto;
        try {
            dto = dataMapper.readValue(rawTransaction, TransactionDTO.class);
        }
        catch(IOException ex) {
            LOGGER.error("Unable to convert data to TransactionDTO " + ex);

            return null;
        }

        return TransactionDTO.toTransaction(dto);
    }
}
