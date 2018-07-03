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
import io.smilo.api.address.AddressManager;
import io.smilo.api.db.Store;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.nio.ByteBuffer.allocateDirect;

@Component
public class BlockStore {

    private static final Logger LOGGER = Logger.getLogger(BlockStore.class);
    private final Store store;
    private List<SmiloChain> chains;
    private static final String COLLECTION_NAME = "block";
    private final ObjectMapper dataMapper;
    private final AddressManager addressManager;

    private long latestBlockHeight = -1;
    private String latestBlockHash = "0000000000000000000000000000000000000000000000000000000000000000";

    public BlockStore(Store store, ObjectMapper dataMapper, AddressManager addressManager) {
        this.addressManager = addressManager;
        this.chains = new ArrayList<>();
        this.store = store;
        this.dataMapper = dataMapper;
        store.initializeCollection(COLLECTION_NAME);
    }

    /**
     * Initialises the height of the latest block
     *
     * @return Boolean
     */
    public void initialiseLatestBlock() {

        // When there is a block in the BlockStore, load the latest.
        // When there is a balance, do nothing, else set 200M Smilo on balance
        if (blockInBlockStoreAvailable()) {
            LOGGER.info("Loading block from DB...");
            Block latestBlock = getLatestBlockFromStore();
            latestBlockHeight = latestBlock.getBlockNum();
            latestBlockHash = latestBlock.getBlockHash();
        } else {
            addressManager.adjustAddressBalance("S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR", 200000000);
            // Todo
        }
    }

    /**
     * Returns the height of the latest block
     *
     * @return int height of latest block
     */
    public long getLatestBlockHeight() {
        return latestBlockHeight;
    }


    /**
     * Returns the hash of the latest block
     *
     * @return String hash the latests added block
     */
    public String getLatestBlockHash() {
        return latestBlockHash;
    }

    /**
     * Set the height of the latest block
     */
    public void setLatestBlockHeight(Long blockHeight) {
        latestBlockHeight = blockHeight;
    }


    /**
     * Set the hash of the latest block
     */
    public void setLatestBlockHash(String hash) {
        latestBlockHash = hash;
    }

    /**
     * Writes a block to the smiloChain file
     *
     * @param block to write
     */
    public void writeBlockToFile(Block block) {
        final ByteBuffer key = allocateDirect(64);
        final ByteBuffer val = allocateDirect(10000);

        try {
            BlockDTO dto = BlockDTO.toDTO(block);
            byte[] bytes = dataMapper.writeValueAsBytes(dto);

            key.putLong(block.getBlockNum()).flip();
            val.put(bytes).flip();
            store.put(COLLECTION_NAME, key, val);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to convert block to byte array " + e);
        }
    }

    /**
     * Get specific block from DB
     * @Integer blockNum
     * @return Block
     */
    public Block getBlock(long blockNum) {
        final ByteBuffer key = allocateDirect(64);
        key.putLong(blockNum).flip();

        byte raw[] = store.get(COLLECTION_NAME, key);
        BlockDTO result;
        try {
            result = dataMapper.readValue(raw, BlockDTO.class);
        } catch (IOException e) {
            LOGGER.error("Unable to convert byte array to block" + e);
            return null;
        }
        return BlockDTO.toBlock(result);
    }

    public Block getLatestBlockFromStore() {
        byte[] raw = store.last(COLLECTION_NAME);
        BlockDTO result = null;
        try {
            result = dataMapper.readValue(raw, BlockDTO.class);
        } catch (IOException e) {
            LOGGER.error("Unable to convert byte array to block" + e);
        }
        return BlockDTO.toBlock(result);
    }

    public Boolean blockInBlockStoreAvailable(){
        if(store.getEntries(COLLECTION_NAME) > 0L){
            LOGGER.debug("Block has an entry");
            return true;
        } else {
            LOGGER.debug("Block has no entries");
            return false;
        }
    }
}