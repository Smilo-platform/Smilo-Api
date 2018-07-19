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

public class Block {
    private String blockHash;
    private int blocknum;
    private String prevBlockHash;
    private Long timestamp;
    private JSONArray transactions;

    public Block (String blockHash, int blocknum, String prevBlockHash, Long timestamp, JSONArray transactions){
        this.blockHash = blockHash;
        this.blocknum = blocknum;
        this.prevBlockHash = prevBlockHash;
        this.timestamp = timestamp;
        this.transactions = transactions;
    }

    public String getBlockHash(){
        return this.blockHash;
    }

    public int getBlocknum(){
        return this.blocknum;
    }

    public String getPrevBlockHash(){
        return this.prevBlockHash;
    }

    public Long getTimestamp(){
        return this.timestamp;
    }

    public JSONArray getTransactions() {
        return transactions;
    }
}
