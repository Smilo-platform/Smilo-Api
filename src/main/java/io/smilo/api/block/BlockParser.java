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
import io.smilo.api.block.data.transaction.Transaction;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.isEmpty;

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

    private String generateBlockData(long timestamp, int blockNum, String previousBlockHash, String signingAddress, String ledgerHash, List<Transaction> transactions) {
        String block = "{" + timestamp + ":" + blockNum + ":" + previousBlockHash + ":" + signingAddress + "},{" + ledgerHash + "},{";
        String transactionString = transactions.stream().filter(Transaction::hasContent).map(Transaction::getRawTransaction).collect(joining("*"));
        block += transactionString + "}";
        return block;
    }

    /**
     * See above for a lot of information. This constructor accepts the raw block format instead of all the arguments separately!
     *
     * @param rawBlock String representing the raw data of a block
     * @return parsed block
     * @throws io.smilo.api.block.BlockParseException thrown when we failed to parse your block
     */
    //TODO: remove this when genesis block is rewritten in DatabaseManager
    public Block parseRawBlock(String rawBlock) throws BlockParseException {
        /*
         * Using a workaround for the unknown number of transactions, which would each be split into multiple parts as they
         * contain a comma as part of the signature. As such, all part up to and including the list of transactions are parsed
         * manually. Then, the remainder can be separated using the split command.
         */
        String[] parts = new String[7];
        parts[0] = rawBlock.substring(0, rawBlock.indexOf("}") + 1); // {timestamp:blocknum:prevBlockHash:nodeAddress}
        rawBlock = rawBlock.substring(rawBlock.indexOf("}") + 2); //Account for comma
        parts[1] = rawBlock.substring(0, rawBlock.indexOf("}") + 1); // {ledgerHash}
        rawBlock = rawBlock.substring(rawBlock.indexOf("}") + 2); //Account for comma, again
        parts[2] = rawBlock.substring(0, rawBlock.indexOf("}") + 1); // {transactions}
        rawBlock = rawBlock.substring(rawBlock.indexOf("}") + 2); //Account for comma a third time
        String[] partsInitial = rawBlock.split(",");
        for (int i = 3; i < 7; i++) {
            parts[i] = partsInitial[i - 3];
        }
        LOGGER.info("Block parts: " + parts.length);
        for (int i = 0; i < parts.length; i++) {
            String toPrint = parts[i];
            if (parts[i].length() > 40) {
                toPrint = parts[i].substring(0, 20) + "..." + parts[i].substring(parts[i].length() - 20);
            }
            LOGGER.info("     " + i + ": " + toPrint);
        }
        String firstPart = parts[0].replace("{", "");
        firstPart = firstPart.replace("}", "");
        String[] firstPartParts = firstPart.split(":"); //Great name, huh?
        try {
            long timestamp = Long.parseLong(firstPartParts[0]);
            int blockNum = Integer.parseInt(firstPartParts[1]);
            String previousBlockHash = firstPartParts[2];
            String redeemAddress = firstPartParts[3];
            String ledgerHash = parts[1].replace("{", "").replace("}", "");
            String transactionsString = parts[2].replace("{", "").replace("}", "");
            List<Transaction> transactions = new ArrayList<>();
            String[] rawTransactions = transactionsString.split("\\*"); //Transactions are separated by an asterisk, as the colon, double-colon, and comma are all used in other places, and would be a pain to use here.
            for (String rawTransaction : rawTransactions) {
                if (isEmpty(rawTransaction)) {
                    LOGGER.info("No transactions found for block!");
                } else {
                    //TODO:fix this
//                    transactions.add(transactionParser.build(rawTransaction));
                }
            }
            //parts[3] is a block hash
            String nodeSignature = parts[4].replace("{", "") + "," + parts[5].replace("}", "");
            int nodeSignatureIndex = Integer.parseInt(parts[6].replace("{", "").replace("}", ""));

            Block block =  new Block(timestamp, blockNum, previousBlockHash, redeemAddress, ledgerHash, transactions, nodeSignature, nodeSignatureIndex);
            block.hashBlock();
            return block;
        } catch (Exception e) {
            throw new BlockParseException(rawBlock, "Something went wrong when parsing a block!", e);
        }
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

        // Todo: Address can not have generated a block in the last x blocks
        // Todo: make this number flexible. should be decided by the amount of nodes available
        try {
            //Recalculate block hash
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            //Prevent empty transaction sets from tripping with a negative substring index
            String blockData = generateBlockData(block.getTimestamp(), block.getBlockNum(), block.getPreviousBlockHash(), block.getRedeemAddress(), block.getLedgerHash(), block.getTransactions());
            String blockHash = DatatypeConverter.printHexBinary(md.digest(blockData.getBytes("UTF-8")));
            //This is the message signed by the block creating node
            String fullBlock = blockData + ",{" + blockHash + "}";
//            if (!addressUtility.verifyMerkleSignature(fullBlock, block.getNodeSignature(), block.getRedeemAddress(), block.getNodeSignatureIndex())) {
//                LOGGER.info("Block didn't verify for " + block.getRedeemAddress() + " with index " + block.getNodeSignatureIndex());
//                LOGGER.info("Signature mismatch error");
//                LOGGER.info("fullBlock: " + fullBlock);
//                LOGGER.info("nodeSignature: " + block.getNodeSignature());
//                return false; //Block mining node signature is not valid
//            }

            // Return true if all transactions are valid
            // TODO: Can be removed if transactions are validated during parsing earlier in the process
            return true;
        } catch (Exception e) {
            LOGGER.error("Oops " + e);
            return false;
        }
    }

    @Override
    public void hash(Block block) {
        try {
            block.setBlockHash(generateDataHash(block.getHashableData().getBytes()));
        } catch (Exception ex) {
            LOGGER.error("Unable to create data hash for block", ex);
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
