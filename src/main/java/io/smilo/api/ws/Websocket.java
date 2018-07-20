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

package io.smilo.api.ws;

import io.smilo.api.block.Block;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionOutput;
import io.smilo.api.cache.BlockCache;
import io.smilo.api.cache.BlockDataCache;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.*;

@Component
@ServerEndpoint("/websocket")
public class Websocket {
    private static final Logger LOGGER = Logger.getLogger(Websocket.class);
    private static final Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());

    private final BlockCache blockCache = new BlockCache();
    private final BlockDataCache blockDataCache = new BlockDataCache();


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
                sendBlockCache();
                break;
            case "GET_LAST_BLOCK_DATA":
                // send PendingBlockDataPool
                sendBlockDataCache();
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

    public JSONObject generateBlockObject(Block block){
        try{
            ArrayList transactions = new ArrayList<>();
            for (Transaction tx : block.getTransactions()){
                transactions.add(generateTxObject(tx));
            }

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

    public void sendBlockCache(){
        try {
            for (Block block :  blockCache.getBlocks().values()){
                sendBlock(block);
            }
        } catch (NullPointerException e){
            LOGGER.warn("BlockCache is empty!");
        }

    }

    public void sendBlock(Block block){
        JSONObject blockObject = generateBlockObject(block);
        sendObject(blockObject, "BLOCK");
    }

    public void sendBlockDataCache(){
        try {
            for (Transaction tx :  blockDataCache.getTransactions().values()){
                sendBlockData(tx);
            }
        } catch (NullPointerException e) {
            LOGGER.warn("BlockDataCache is empty!");
        }
    }

    public void sendBlockData(Transaction tx){
        JSONObject txObject = generateTxObject(tx);
        sendObject(txObject, "BLOCK_DATA");
    }

    public void sendPendingBlockData(Transaction tx){
        JSONObject txObject = generateTxObject(tx);
        sendObject(txObject, "PENDING_BLOCK_DATA");
    }
}