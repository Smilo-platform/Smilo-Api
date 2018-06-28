package io.smilo.api.demo;

import io.smilo.api.ws.Websocket;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class ScheduledTasks {

    @Autowired
    private Websocket websocket;
    private static final Logger LOGGER = Logger.getLogger(ScheduledTasks.class);
    private Block lastBlock;
    private static List<Transaction> txPool = new ArrayList<>(); // Todo solve this

    //@Scheduled(fixedRate = 16000) // TODO enable/fix blocks
    public void sendBlock(){

        Block block;
        if(lastBlock == null){
            // Genesis block
            block = new Block(createHash(), 0, "0000000000000000000000000000000000000000000000000000000000000000", System.currentTimeMillis(), getTxFromPool());
        }else{
            block = new Block(createHash(), lastBlock.getBlocknum()+1, lastBlock.getBlockHash(), System.currentTimeMillis(), getTxFromPool());
        }

        try{
            JSONObject blockObject = new JSONObject();
            blockObject.put("blockHash", block.getBlockHash());
            blockObject.put("blocknum", block.getBlocknum());
            blockObject.put("prevBlockHash", block.getPrevBlockHash());
            blockObject.put("timestamp", block.getTimestamp());
            blockObject.put("transactions", block.getTransactions());

            // Set currentBlock as lastBlock
            lastBlock = block;

            sendMsg(blockObject, "msgBlock");
        }catch (Exception ex){
            LOGGER.error("Failed generating block object");
        }
    }

    @Scheduled(fixedRate = 4500)
    public void generateTransaction(){
        Random rand = new Random();
        int inputAmount = rand.nextInt(5000) + 1;
        double fee = 0.001;
        int assetId = 1;

        Transaction tx = new Transaction(System.currentTimeMillis(), assetId, getRandomAddress(), inputAmount, fee, createHash());
        tx.setTxOutput(addTxOutputData(tx.getInputAmount())); // TODO solve this

        // Add transaction to pool
        txPool.add(tx);

        // Send transaction
        sendMsg(getTxObject(tx), "msgTx");
    }

    public JSONObject getTxObject(Transaction tx){
        try {
            JSONObject txObject = new JSONObject();
            txObject.put("timestamp", tx.getTimestamp());
            txObject.put("assetID", tx.getAssetId());
            txObject.put("inputAddress", tx.getInputAddress());
            txObject.put("inputAmount", tx.getInputAmount());
            txObject.put("txOutputArray", tx.getTxOutput());
            txObject.put("txFee", tx.getFee());
            txObject.put("hash", tx.getHash());
            txObject.put("signatureData", "signatureData"); // Todo add signatureData
            txObject.put("signatureIndex", 0); // Todo add signatureIndex
            txObject.put("txStatus","pending"); // Todo Remove?

            return txObject;
        }catch(Exception ex){
            LOGGER.error("Failed generating transaction object");
        }
        return null;
    }

    public String getRandomAddress() {
        String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
        StringBuilder address = new StringBuilder();
        Random rnd = new Random();
        while (address.length() < 36) {
            int index = (int) (rnd.nextFloat() * charset.length());
            address.append(charset.charAt(index));
        }
        return "S1" + address.toString();
    }

    public String createHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String data = String.valueOf(System.currentTimeMillis());
            return DatatypeConverter.printHexBinary(md.digest(data.getBytes()));
        } catch (Exception e){
            LOGGER.error("Oops...");
        }
        return null;
    }

    public JSONArray addTxOutputData(int inputAmount){
        try {
            JSONArray txOutputs = new JSONArray();
            Random rand = new Random();
            int amountOfTx = rand.nextInt(6) + 1;
            int totalValue = inputAmount;

            for (int i = 0; amountOfTx >= 0; amountOfTx--) {
                JSONObject txOutputObject  = new JSONObject();
                String newOutputAddress = getRandomAddress();

                // Generate new output amount
                int newOutputAmount = rand.nextInt(totalValue) + 1;
                totalValue -= newOutputAmount;

                // Spend everything on last transaction
                if (amountOfTx == 0) {
                    newOutputAmount += totalValue;
                }

                if(newOutputAmount > 0){
                    txOutputObject.put("address", newOutputAddress);
                    txOutputObject.put("value", newOutputAmount);
                    txOutputs.add(txOutputObject);
                }
            }

            return txOutputs;
        }catch(Exception ex){
            LOGGER.error("Generating test transactions failed");
        }
        return null;
    }

    public JSONArray getTxFromPool(){
        try{
            JSONArray txArray = new JSONArray();

            int i = 0;
            for(Transaction tx : txPool){
                JSONObject transactions  = new JSONObject();
                transactions.put(i, getTxObject(tx));
                i++;
            }
            // Empty transaction pool
            txPool = null;

            return txArray;
        }catch (Exception ex){
            LOGGER.error("Reading transaction pool failed");
        }
        return null;
    }

    public void sendMsg(JSONObject message, String type){
        try {
            JSONObject obj = new JSONObject();
            obj.remove("type");
            obj.remove("data");
            obj.put("data", message);
            obj.put("type", type);

            LOGGER.info(obj);
            websocket.sendMessage(obj.toString());
        }catch (Exception e){
            LOGGER.error("Sending message failed");
        }
    }

}
