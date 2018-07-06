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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smilo.api.block.data.BlockDataParser;
import io.smilo.api.block.data.Parser;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * https://smilo-platform.atlassian.net/wiki/spaces/SP/pages/96305164/Blocks TODO: refactor
 *
 * This class will generate and sign a new block. Block format is explained in the wiki.
 */
@Component
//TODO: refactor and write implementation for methods
public class BlockParser extends BlockDataParser implements Parser<Block> {

    private static final Logger LOGGER = Logger.getLogger(BlockParser.class);
    private final ObjectMapper dataMapper;

    public BlockParser(ObjectMapper dataMapper) {
        this.dataMapper = dataMapper;
    }

    @Override
    /**
     * Used to check a variety of conditions to ensure that a block is valid. Valid block requirements: -'Compiled' block format is signed correctly by node -Transactions are formatted correctly
     *
     * @param block block to be validated
     * @return boolean Whether the self-contained block is valid. Does not represent inclusion in the network, or existence of the previous block.
     */
    // TODO: return exceptions instead of boolean
    public boolean isValid(Block block) {
        LOGGER.info("Validating block " + block.getBlockNum());

        if (block.hasNoExplicitTransactions()) {
            //Block has no explicit transactions
            return true;
        }

        // Todo: Create default
        try {
            // Return true if all transactions are valid
            return true;
        } catch (Exception e) {
            LOGGER.error("Oops " + e);
            return false;
        }
    }

    @Override
    public Block deserialize(byte[] raw) {
        Block block = null;
        try {
            BlockDTO dto = dataMapper.readValue(raw, BlockDTO.class);
            block = BlockDTO.toBlock(dto);
        } catch (IOException ex) {
            LOGGER.error("Unable to deserialize block", ex);
        }
        return block;
    }

    @Override
    public byte[] serialize(Block block) {
        byte[] bytes = null;
        try {
            bytes = dataMapper.writeValueAsBytes(BlockDTO.toDTO(block));
        } catch (IOException ex) {
            LOGGER.error("Unable to serialize block", ex);
        }
        return bytes;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Block.class.isAssignableFrom(clazz);
    }
}
