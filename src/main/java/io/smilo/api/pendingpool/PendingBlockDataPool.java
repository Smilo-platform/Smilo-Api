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

package io.smilo.api.pendingpool;

import io.smilo.api.block.AddResultType;
import io.smilo.api.block.Block;
import io.smilo.api.block.ParserProvider;
import io.smilo.api.block.data.AddBlockDataResult;
import io.smilo.api.block.data.BlockData;
import io.smilo.api.block.data.BlockDataParser;
import io.smilo.api.block.data.Parser;
import io.smilo.api.block.data.message.Message;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionOutput;
import io.smilo.api.cache.BlockDataCache;
import io.smilo.api.ws.Websocket;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class PendingBlockDataPool {

    private static final Logger LOGGER = Logger.getLogger(PendingBlockDataPool.class);
    private Set<BlockData> pendingBlockData;
    private final ParserProvider parserProvider;
    private Websocket websocket;
    private BlockDataCache blockDataCache;

    public PendingBlockDataPool(ParserProvider parserProvider, Websocket websocket, BlockDataCache blockDataCache) {
        this.parserProvider = parserProvider;
        this.websocket = websocket;
        this.blockDataCache = blockDataCache;
        pendingBlockData = new HashSet<>();
    }

    public void addMessage(String rawMessage) {
        Parser parser = parserProvider.getParser(Message.class);
        Message message = (Message) parser.deserialize(BlockDataParser.decode(rawMessage));
        addBlockData(message);
    }

    public void addTransaction(String rawTransaction) {
        Parser parser = parserProvider.getParser(Transaction.class);
        Transaction transaction = (Transaction) parser.deserialize(BlockDataParser.decode(rawTransaction));
        addBlockData(transaction);
        blockDataCache.addTransaction(transaction);
        websocket.sendBlockData(transaction);

    }

    public AddBlockDataResult addBlockData(BlockData blockData) {
        try {
            Parser parser = parserProvider.getParser(blockData.getClass());
            Boolean alreadyExists = pendingBlockData.contains(blockData);

            if(alreadyExists) {
                return new AddBlockDataResult(blockData, AddResultType.DUPLICATE, blockData.getClass().getSimpleName() + " is already pending");
            }

            if (!parser.isValid(blockData)) {
                LOGGER.info("Throwing out a message deemed invalid");
                return new AddBlockDataResult(blockData, AddResultType.VALIDATION_ERROR, "Throwing out a " + blockData.getClass().getSimpleName() + " deemed invalid");
            }

            // Todo: Do something!

            return new AddBlockDataResult(blockData, AddResultType.ADDED, "Added " + blockData.getClass().getSimpleName());
        } catch (Exception e) {
            LOGGER.error("An exception has occurred..." + e);
            return new AddBlockDataResult(blockData, AddResultType.UNKNOWN, "An exception has occurred");
        }
    }

    /**
     * Removes identical block data from the pending block data pool
     *
     * @param blockData The transaction to remove
     *
     * @return boolean Whether removal was successful
     */
    public boolean removeBlockData(BlockData blockData) {
        return pendingBlockData.remove(blockData);
    }

    /**
     * This method is the most useful method in this class--it allows the mass removal of all transactions from the pending transaction pool that were included in a network block, all in one call. The
     * returned boolean is not currently utilized in MainClass, proper handling of blocks with transaction issues will be addressed in a future alpha, probably 0.0.1a6/7 given my schedule.
     *
     * @param block The block holding transactions to remove
     *
     * @return boolean Whether all transactions in the block were successfully removed
     */
    public boolean removeTransactionsInBlock(Block block) {
        //This try-catch method wraps around more than it needs to, in the name of easy code management, and making colors line up nicely in my IDE.
        try {
            /* Transaction format:
             * InputAddress;InputAmount;OutputAddress1;OutputAmount1;OutputAddress2;OutputAmount2...;SignatureData;SignatureIndex
             *
             * We are removing only transactions that match the exact String from the block. If the block validation fails, NO transactions are removed from the pool.
             * In a late-night coding session, not removing any transactions of an invalid block seemed like the bset idea--transactions should never be discarded
             * if they haven't made it into the blockchain, and any block that doesn't isValid won't make it through Blockchain's block screening, so these transactions
             * that we aren't removing will never happen on-chain if we remove them from the pool when an invalid block says we should. Also closes a potential attack
             * vector where someone could submit false blocks in order to be a nuisance and empty the pending transaction pool.
             */
            List<Transaction> transactions = block.getTransactions();
            boolean allSuccessful = true;
            for (int i = 0; i < transactions.size(); i++) {
                if (!removeBlockData(transactions.get(i))) {
                    allSuccessful = false; //This might happen if a transaction was in a block before it made it across the network to a peer, so not always a big deal!
                }
            }
            return allSuccessful;
        } catch (Exception e) {
            LOGGER.error("Oops " + e);
            return false;
        }
    }

    /**
     * This method scans through all of the pending transactions to calculate the total (net) balance change pending on an address. A negative value represents coins that were sent from the address in
     * question, and a positive value represents coins awaiting confirmations to arrive.
     *
     * @param address Smilo address to search the pending transaction pool for
     *
     * @return long The pending total (net) change for the address in question
     */
    public long getPendingBalance(String address) {
        long totalChange = 0L;
        List<Transaction> pendingTransactionsss = getPendingData(Transaction.class);

        for (int i = 0; i < pendingTransactionsss.size(); i++) {
            Transaction transaction =  pendingTransactionsss.get(i);
            try {
                if (transaction.containsAddress(address)) {
                    String senderAddress = transaction.getInputAddress();
                    if (senderAddress.equals(address)) {
                        totalChange -= transaction.getInputAmount();
                    }
                    totalChange += transaction.getTransactionOutputs().stream()
                            .filter(txOutput -> txOutput.getOutputAddress().equals(address))
                            .mapToLong(TransactionOutput::getOutputAmount)
                            .sum();
                }
            } catch (Exception e) {
                LOGGER.error("Major problem: Transaction in the pending transaction pool is incorrectly formatted!");
                LOGGER.error("Transaction in question: " + transaction);
                LOGGER.debug("Stacktrace: " + e);
            }
        }
        return totalChange;
    }

    public <T extends BlockData> List<T> getPendingData(Class<T> clazz) {
        List<T> data = new ArrayList<>();
        pendingBlockData.stream().filter(b -> b.getClass().equals(clazz)).forEach(b -> data.add((T) b));
        return data;
    }

    public Set<BlockData> getPendingBlockData() {
        return pendingBlockData;
    }
}

