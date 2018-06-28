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
