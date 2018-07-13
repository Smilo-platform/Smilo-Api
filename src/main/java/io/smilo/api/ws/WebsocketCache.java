package io.smilo.api.ws;

import io.smilo.api.block.Block;
import io.smilo.api.block.data.transaction.Transaction;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;
import org.apache.log4j.Logger;

import java.util.*;

@Component
public class WebsocketCache {

    private final Map<Long, Block> blocks = new HashMap<>();
    private final Map<String, Transaction> transactions = new HashMap<>();
    private static final Websocket websocket = new Websocket();
    private static final Logger LOGGER = Logger.getLogger(Websocket.class);

    public void addLatestBlock(Block block){
        this.blocks.putIfAbsent(block.getBlockNum(), block);
        while(this.blocks.size() >= 26){
            this.blocks.remove(block.getBlockNum() - 25);
        }
    }

    public void sendBlockCache(){
        for (Block block : blocks.values()){
            JSONObject blockObject = websocket.generateBlockObject(block);
            websocket.sendObject(blockObject, "msgBlock");
        }
    }

    public void addLatestTransaction(Transaction tx){
        this.transactions.putIfAbsent(tx.getDataHash(), tx);
        while(this.transactions.size() >= 26){
            this.transactions.remove(0);
        }
    }

    public void sendTxCache(){
        for(Transaction tx: transactions.values()){
            JSONObject txObject = websocket.generateTxObject(tx);
            websocket.sendObject(txObject, "msgTx");
        }
    }
}