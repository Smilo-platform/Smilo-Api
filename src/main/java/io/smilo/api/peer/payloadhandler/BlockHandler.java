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

package io.smilo.api.peer.payloadhandler;

import io.smilo.api.block.Block;
import io.smilo.api.block.BlockParser;
import io.smilo.api.block.BlockStore;
import io.smilo.api.block.data.BlockDataParser;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionStore;
import io.smilo.api.peer.NetworkState;
import io.smilo.api.peer.Peer;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BlockHandler implements PayloadHandler {

    private PendingBlockDataPool pendingBlockDataPool;
    private BlockParser blockParser;
    private BlockStore blockStore;
    private TransactionStore transactionStore;
    private NetworkState networkState;

    private static final Logger LOGGER = Logger.getLogger(BlockHandler.class);

    public BlockHandler(PendingBlockDataPool pendingBlockDataPool, BlockParser blockParser,
                        BlockStore blockStore, NetworkState networkState,
                        TransactionStore transactionStore) {
        this.pendingBlockDataPool = pendingBlockDataPool;
        this.blockParser = blockParser;
        this.blockStore = blockStore;
        this.networkState = networkState;
        this.transactionStore = transactionStore;
    }

    @Override
    public void handlePeerPayload(List<String> parts, Peer peer) {
        byte[] byteArray = BlockDataParser.decode(parts.get(1));
        Block block = blockParser.deserialize(byteArray);

        // Todo: include block in database.
        // Todo: include block in last 10 blocks list
        // Todo: update latestBlock

        // Topblock = 1 (From network)
        // Last block = 1
        // Blockchain length equals new blocks BlockNum when this is the first next block
        if ((blockStore.getLatestBlockHeight() +1) == block.getBlockNum() &&
                blockStore.getLatestBlockHash().equals(block.getPreviousBlockHash())){
            // Add Block
            LOGGER.info("Previous Block Height: " + blockStore.getLatestBlockHeight());
            LOGGER.info("Added Block " + block.getBlockNum() + " to the database.");
            LOGGER.info("Hash: " + block.getBlockHash());
            LOGGER.info("Current Block heigth: " + (blockStore.getLatestBlockHeight() + 1));

            blockStore.setLatestBlockHash(block.getBlockHash());
            blockStore.setLatestBlockHeight(block.getBlockNum());
            try {
                blockStore.writeBlockToFile(block);
                // Todo:
                // Write BlockData to disk/mem
                // Update balance

            } catch(Exception e) {
                LOGGER.error("Writing block " + block.getBlockNum() + " to database failed!");
            }

            storeTransactions(block.getTransactions());

            networkState.updateCatchupMode();

        } else if ((blockStore.getLatestBlockHeight() +1) < block.getBlockNum()){
            // Todo: put in a BlockQueue and parse later.
            LOGGER.info("This is a block from the future.. REQUEST_NET_STATE Needed.");
            if(block.getBlockNum() > networkState.getTopBlock()){
                networkState.setTopBlock(block.getBlockNum()+1);
            }
        } else if (blockStore.getLatestBlockHeight() == block.getBlockNum()) {
            LOGGER.debug("Just parsed block " + block.getBlockNum() + ". Dropping.");
        } else if ((blockStore.getLatestBlockHeight()+1) > block.getBlockNum()) {
            LOGGER.info("Dr. Emmett Brown: You’ve got to come back with me!\n" +
                    "Marty McFly: Where?\n" +
                    "Dr. Emmett Brown: Back to the future!");
            LOGGER.error("Old followup block. Dropping.");
        } else {
            LOGGER.warn("Wrong followup block " + block.getBlockNum() + ". Dropping.");
            LOGGER.debug("Block: " + block.getBlockNum() + " " + block.getBlockHash());
            LOGGER.debug("Should be: " + (blockStore.getLatestBlockHeight() + 1));
        }

        //Remove all transactions from the pendingTransactionPool that appear in the block
        pendingBlockDataPool.removeTransactionsInBlock(block);
    }

    private void storeTransactions(List<Transaction> transactions) {
        for(Transaction transaction : transactions) {
            LOGGER.info("Write transaction " + transaction.getDataHash() + " to lmdb...");
            transactionStore.writeTransactionToFile(transaction);
            LOGGER.info("wrote transaction!");
        }

        // Try and read transactions
        for(Transaction transaction : transactions) {
            LOGGER.info("Try get transaction");
            Transaction other = transactionStore.getTransaction(transaction.getDataHash());

            if(other != null) {
                LOGGER.info("Could read transactions!");
            }
            else {
                LOGGER.error("Could not read transaction!");
            }
        }
    }

    @Override
    public PayloadType supports() {
        return PayloadType.BLOCK;
    }
}

