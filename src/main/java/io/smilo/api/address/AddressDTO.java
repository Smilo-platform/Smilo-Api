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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigInteger;
import java.util.Map;

public class AddressDTO {

    private String address;
    @JsonSerialize(using = AddressBalancesSerializer.class)
    private Map<String, BigInteger> balances;
    private long signatureCount;

    public AddressDTO() {
        // Make sonar happy :)
    }

    public AddressDTO(String address, Map<String, BigInteger> contractBalanceMap, long signatureCount) {
        this.address = address;
        this.balances = contractBalanceMap;
        this.signatureCount = signatureCount;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getAddress() {
        return address;
    }
    
    public Map<String, BigInteger> getBalances() {
        return balances;
    }

    public BigInteger getBalance(String assetId) {
        if(this.balances.containsKey(assetId)) {
            return this.balances.get(assetId);
        }
        else {
            return BigInteger.ZERO;
        }
    }
    
    public void setBalances(Map<String, BigInteger> balance) {
        this.balances = balance;
    }

    public long getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }

    public static AddressDTO toDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setAddress(address.getAddress());
        dto.setBalances(address.getBalances());
        dto.setSignatureCount(address.getSignatureCount());
        return dto;
    }

    public static Address toAddress(AddressDTO dto) {
        Address address = new Address();
        address.setAddress(dto.getAddress());
        address.setBalances(dto.getBalances());
        address.setSignatureCount(dto.getSignatureCount());
        return address;
    }
    
}
