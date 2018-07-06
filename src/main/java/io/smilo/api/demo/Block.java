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
