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

import java.util.HashMap;
import java.util.Map;

public class Address {

    private String publickey;
    /**
     * We use a map with string as key and double as value. Even though XSM will never have a decimal value
     * we still use double because all other tokens could be decimal. Applications implementing this API
     * should account for this.
     */
    private Map<String, Double> balances = new HashMap<String, Double>();
    private long signatureCount;

    public Address() {
    }

    public Address(String address, Map<String, Double> contractBalanceMap, long signatureCount) {
        this.publickey = address;
        this.balances = contractBalanceMap;
        this.signatureCount = signatureCount;
    }

    public void setAddress(String address) {
        this.publickey = address;
    }
    
    public String getAddress() {
        return publickey;
    }

    public Map<String, Double> getBalances() {
        return this.balances;
    }

    public void setBalances(Map<String, Double> balances) {
        this.balances = balances;
    }
    
    public double getBalance(String contract) {
        return balances.get(contract);
    }

    /**
     * Increments the balance for the given contract. If the contract was not found the balance is simply set to the given amount.
     * @param contract
     * @param increment
     */
    public void incrementBalance(String contract, double increment) {
        if(this.balances.containsKey(contract))
            this.balances.put(contract, this.balances.get(contract) + increment);
        else
            this.balances.put(contract, increment);
    }

    public void decrementBalance(String contract, double decrement) {
        incrementBalance(contract, -decrement);
    }
    
    public void setBalance(String contract, double balance) {
        this.balances.put(contract, balance);
    }

    public long getSignatureCount() {
        return signatureCount;
    }

    public void setSignatureCount(long signatureCount) {
        this.signatureCount = signatureCount;
    }
    
}
