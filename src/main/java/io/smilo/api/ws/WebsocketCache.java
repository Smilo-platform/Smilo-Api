package io.smilo.api.ws;

import io.smilo.api.block.Block;
import io.smilo.api.block.data.transaction.Transaction;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class WebsocketCache {

    private Map<Long, Block> blocks;
    private List<Transaction> transactions;

    public WebsocketCache() {
        this.blocks = new HashMap<>();
        this.transactions = null;
    }

    public void addLatestBlock(Block block){
        this.blocks.putIfAbsent(block.getBlockNum(), block);
        if (this.blocks.size() >= 26){
            this.blocks.remove(block.getBlockNum() - 25);
        }
    }
}
