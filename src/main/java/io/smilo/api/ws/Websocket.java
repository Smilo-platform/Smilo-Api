package io.smilo.api.ws;

import io.smilo.api.block.Block;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionOutput;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.*;
import java.util.stream.Collectors;

@Component
@ServerEndpoint("/websocket")
public class Websocket {

    private static final WebsocketCache websocketCache = new WebsocketCache();

    private static final Logger LOGGER = Logger.getLogger(Websocket.class);

    private static final Set<Session> clients =
            Collections.synchronizedSet(new HashSet<Session>());

    public void broadcastMessage(String message) {
        synchronized(clients){
            // Iterate over the connected sessions
            // and broadcast the received message
            for(Session client : clients){
                try {
                    client.getBasicRemote().sendText(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendMessage(String message, Session session) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen (Session session) {
        // Add session to the connected sessions set
        clients.add(session);
        sendMessage("Welcome to the Smilo Api!", session);
    }

    @OnMessage
    public void onMessage (String message, Session session){
        switch (message) {
            case "GET_LAST_BLOCKS":
                websocketCache.sendBlockCache();
                break;
            case "GET_LAST_TRANSACTIONS":
                websocketCache.sendTxCache();
                break;
        }

        LOGGER.info("Revieved request: " + message);
        sendMessage("Response: " + message, session);
    }

    @OnClose
    public void onClose (Session session) {
        // Remove session from the connected sessions set
        clients.remove(session);
    }

    public void addBlock(Block block){
        websocketCache.addLatestBlock(block);
        JSONObject blockObject = generateBlockObject(block);
        sendObject(blockObject, "msgBlock");
    }

    public void addTransaction(Transaction tx){
        websocketCache.addLatestTransaction(tx);
        JSONObject txObject = generateTxObject(tx);
        sendObject(txObject, "msgTx");
    }

    public JSONObject generateBlockObject(Block block){
        try{
            // TODO remove both lines
            ArrayList transactions = new ArrayList<>();
            for (Transaction tx : block.getTransactions()){
                transactions.add(generateTxObject(tx));
            }

            // TODO foreach transaction in block.getTransactions, create JSONObject (same as with generateTxObject)

            JSONObject blockObject = new JSONObject();
            blockObject.put("blockHash", block.getBlockHash());
            blockObject.put("blocknum", block.getBlockNum());
            blockObject.put("prevBlockHash", block.getPreviousBlockHash());
            blockObject.put("timestamp", block.getTimestamp());
            blockObject.put("transactions", transactions);

            return blockObject;
        }catch (Exception ex){
            LOGGER.error("Failed generating block object");
            return null;
        }
    }

    public JSONObject generateTxObject(Transaction tx){
        try {
            ArrayList transactionOutputs = new ArrayList();
            JSONObject txObject = new JSONObject();

            // TODO foreach transaction in tx.getTransactionOutputs(); (increment on the zero)
            for (TransactionOutput txOut : tx.getTransactionOutputs()) {
                JSONObject transactionOutput = new JSONObject();
                transactionOutput.put("outputAddress", txOut.getOutputAddress());
                transactionOutput.put("outputAmount", txOut.getOutputAmount());
                transactionOutputs.add(transactionOutput);
            }

            txObject.put("timestamp", tx.getTimestamp());
            txObject.put("assetID", tx.getAssetId());
            txObject.put("inputAddress", tx.getInputAddress());
            txObject.put("inputAmount", tx.getInputAmount());
            txObject.put("txOutputArray",transactionOutputs);
            txObject.put("txFee", tx.getFee());
            txObject.put("hash", tx.getDataHash());
            txObject.put("signatureData", tx.getSignatureData());
            txObject.put("signatureIndex", tx.getSignatureIndex());

            return txObject;
        }catch(Exception ex){
            LOGGER.error("Failed generating transaction object");
            return null;
        }
    }

    public void sendObject(JSONObject message, String type){
        try {
            JSONObject obj = new JSONObject();
            obj.remove("type");
            obj.remove("data");
            obj.put("data", message);
            obj.put("type", type);

            broadcastMessage(obj.toString());
        }catch (Exception e){
            LOGGER.error("Sending message failed");
        }
    }
}