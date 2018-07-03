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

public class AddressDTO {

    private String address;
    private long balance;
    private int signatureCount;

    public AddressDTO() {
    }

    public AddressDTO(String address, long balance, int signatureCount) {
        this.address = address;
        this.balance = balance;
        this.signatureCount = signatureCount;
    }

    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getAddress() {
        return address;
    }
    
    public long getBalance() {
        return balance;
    }
    
    public void setBalance(long balance) {
        this.balance = balance;
    }

    public int getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(int signatureCount) {
        this.signatureCount = signatureCount;
    }

    public static AddressDTO toDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setAddress(address.getAddress());
        dto.setBalance(address.getBalance());
        dto.setSignatureCount(address.getSignatureCount());
        return dto;
    }

    public static Address toAddress(AddressDTO dto) {
        Address address = new Address();
        address.setAddress(dto.getAddress());
        address.setBalance(dto.getBalance());
        address.setSignatureCount(dto.getSignatureCount());
        return address;
    }
    
}
