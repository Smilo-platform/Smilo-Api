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

package io.smilo.api.block;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smilo.api.db.Store;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.ByteBuffer.allocateDirect;

@Component
public class BlockStore {

    private static final Logger LOGGER = Logger.getLogger(BlockStore.class);
    private final Store store;
    private List<SmiloChain> chains;
    private static final String COLLECTION_NAME = "block";
    private final ObjectMapper dataMapper;

    public BlockStore(Store store,  ObjectMapper dataMapper) {
        this.chains = new ArrayList<>();
        this.store = store;
        this.dataMapper = dataMapper;
        store.initializeCollection(COLLECTION_NAME);
    }

//    /**
//     * Retrieves the block at blockNum (starting from 0) from the longest chain.
//     *
//     * @param blockNum The block number to retrieve
//     *
//     * @return Block Block at blockNum in longest chain
//     */
//    public Block getBlock(int blockNum) {
//        return getLargestChain().getBlockByIndex(blockNum);
//    }

    // TODO: return optional instead of null
    public SmiloChain getLargestChain() {
        return chains.stream()
                .max((a, b) -> Integer.compare(a.getLength(), b.getLength()))
                .orElse(new SmiloChain());
    }

    /**
     * Returns the length of the tallest tree
     *
     * @return int Length of longest tree in smiloChain
     */
    public int getBlockchainLength() {
        return getLargestChain().getLength();
    }

    /**
     * Writes a block to the smiloChain file
     *
     * @param block to write
     */
    public void writeBlockToFile(Block block) {
        final ByteBuffer key = allocateDirect(10000);
        final ByteBuffer val = allocateDirect(10000);

        try {
            BlockDTO dto = BlockDTO.toDTO(block);
            byte[] bytes = dataMapper.writeValueAsBytes(dto);

            key.put(Byte.parseByte(String.valueOf(block.getBlockNum()))).flip();
            val.put(bytes).flip();
            store.put(COLLECTION_NAME, key, val);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to convert block to byte array " + e);
        }
    }

    /**
     * Saves entire smiloChain to a file, useful to save the state of the smiloChain so it doesn't have to be redownloaded later. Blockchain is stored to a file called SMILOCHAIN_DATA inside the
     * provided dbFolder.
     *
     *
     * @return boolean Whether saving to file was successful.
     */
    public void saveToFile() {
        this.chains.forEach(chain -> {
            chain.getBlocks().forEach(this::writeBlockToFile);
        });
    }

    /**
     * Calls getTransactionsInvolvingAddress() on all Block objects in the current Blockchain to get all relevant transactions.
     *
     * @param addressToFind Address to search through all block transaction pools for
     *
     * @return ArrayList<String> All transactions in simplified form blocknum:sender:amount:asset:receiver of
     */
    //TODO: refactor
    public ArrayList<String> getAllTransactionsInvolvingAddress(String addressToFind) {
        SmiloChain longestChain = getLargestChain();

        ArrayList<String> allTransactions = new ArrayList<>();

        for (int i = 0; i < longestChain.getLength(); i++) {
            ArrayList<String> transactionsFromBlock = longestChain.getBlockByIndex(i).getTransactionsInvolvingAddress(addressToFind);
            for (int j = 0; j < transactionsFromBlock.size(); j++) {
                allTransactions.add(longestChain.getBlockByIndex(i).getBlockNum() + ":" + transactionsFromBlock.get(j));
            }
        }
        return allTransactions;
    }

    /**
     * Returns the last block of the largest chain
     *
     * @return the last block of the largest chain
     */
    public Block getLastBlock() {
        SmiloChain chain = getLargestChain();
        return chain.getLastBlock().orElse(null);
    }

    public List<SmiloChain> getAll() {
        return this.chains;
    }

    public boolean containsHash(String blockHash) {
        return chains.stream()
                .anyMatch(chain -> chain.containsHash(blockHash));
    }

    public void cleanUpChains() {
        chains = chains.stream().filter(c -> c.getLength() > getBlockchainLength() - 10).collect(Collectors.toList());
    }

    public void addSmiloChain(SmiloChain initial) {
        chains.add(initial);
    }

    public Block getBlock(int blockNum) {
        final ByteBuffer key = allocateDirect(100000);
        key.put(Byte.parseByte(String.valueOf(blockNum))).flip();

        byte raw[] = store.get(COLLECTION_NAME, key);
        BlockDTO result = null;
        try {
            result = dataMapper.readValue(raw, BlockDTO.class);
        } catch (IOException e) {
            LOGGER.error("Unable to convert byte array to block" + e);
        }
        return BlockDTO.toBlock(result);
    }
}