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

package io.smilo.api.demo;

import org.json.simple.JSONArray;

public class Transaction {

    private Long timestamp;
    private int assetId;
    private String inputAddress;
    private int inputAmount;
    private JSONArray txOutput;
    private double fee;
    private String hash;

    public Transaction(Long timestamp, int assetId, String inputAddress, int inputAmount, double fee, String hash){
        this.timestamp = timestamp;
        this.assetId = assetId;
        this.inputAddress = inputAddress;
        this.inputAmount = inputAmount;
        this.fee = fee;
        this.hash = hash;
    }

    public int getInputAmount(){
        return this.inputAmount;
    }

    public int getAssetId(){
        return this.assetId;
    }

    public Long getTimestamp(){
        return this.timestamp;
    }

    public String getInputAddress(){
        return this.inputAddress;
    }

    public void setTxOutput(JSONArray txOutputString){
        this.txOutput = txOutputString;
    }

    public JSONArray getTxOutput(){
        return this.txOutput;
    }

    public double getFee(){
        return this. fee;
    }

    public String getHash(){
        return this.hash;
    }
}
