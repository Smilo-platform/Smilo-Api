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
import io.smilo.api.block.data.transaction.TransactionParser;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

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
    private final TransactionParser transactionParser;
    private static final byte CURRENT_VERSION = (byte) 1;

    public BlockParser(ObjectMapper dataMapper, TransactionParser transactionParser) {
        this.dataMapper = dataMapper;
        this.transactionParser = transactionParser;
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

    //     * @param timestamp Timestamp originally set into the block by the node
//     * @param blockNum The block number
//     * @param previousBlockHash The hash of the previous block
//     * @param redeemAddress the user's public key
//     * @param ledgerHash The hash of the ledger as it existed before this block's transactions occurred
//     * @param transactions List<Transaction> of all the transactions included in the block
//     * @param nodeSignature Node's signature of the block
//     * @param nodeSignatureIndex Node's signature index used when generating nodeSignature
    @Override
    public Block deserialize(byte[] raw) {
        if (raw.length == 0) return null;
        MessageUnpacker msgpack = MessagePack.newDefaultUnpacker(raw);
        Block block;
        try {
            msgpack.unpackByte(); // Skip version number
            Long timestamp = msgpack.unpackLong();
            Long blockNumber = msgpack.unpackLong();
            String previousBlockHash = msgpack.unpackString();
            String redeemAddress = msgpack.unpackString();
            String ledgerHash = msgpack.unpackString();
            int numberOfTransactions = msgpack.unpackArrayHeader();
            List<Transaction> transactions = new ArrayList<>();
            for (int i = 0; i < numberOfTransactions; i++) {
                int sizeOfTransaction = msgpack.unpackBinaryHeader();
                byte[] rawTransaction = new byte[sizeOfTransaction];
                msgpack.readPayload(rawTransaction);
                transactions.add(transactionParser.deserialize(rawTransaction));
            }
            int size = msgpack.unpackBinaryHeader();
            byte [] extraData = msgpack.readPayload(size);
            LOGGER.info("Unsupported extra data inside: " + Hex.encodeHexString(extraData));
            String blockHash = msgpack.unpackString();
            String blockSignature = msgpack.unpackString();
            Long blockSignatureIndex = msgpack.unpackLong();
            block = new Block(timestamp,blockNumber,previousBlockHash,redeemAddress,ledgerHash,transactions,blockSignature,blockSignatureIndex.intValue());
            block.setBlockHash(blockHash);
        } catch (IndexOutOfBoundsException | IOException ex) {
            LOGGER.error("Unable to deserialize block", ex);
            LOGGER.debug("Mensagem: "+ new String(raw));

            return null;
        }
        return block;
    }

    @Override
    public byte[] serialize(Block block) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msgpack = MessagePack.newDefaultPacker(out)) {
            msgpack.packByte(CURRENT_VERSION);
            msgpack.packLong(block.getTimestamp());
            msgpack.packLong(block.getBlockNum());
            msgpack.packString(block.getPreviousBlockHash());
            msgpack.packString(block.getRedeemAddress());
            msgpack.packString(block.getLedgerHash());
            List<Transaction> transactions = block.getTransactions();
            msgpack.packArrayHeader(transactions.size());
            for (Transaction tx : transactions) {
                byte[] rawTransaction = transactionParser.serialize(tx);
                msgpack.packBinaryHeader(rawTransaction.length);
                msgpack.addPayload(rawTransaction);
            }
            msgpack.packBinaryHeader(0);
            msgpack.packString(block.getBlockHash());
            msgpack.packString(block.getNodeSignature());
            msgpack.packLong(block.getNodeSignatureIndex());
            msgpack.flush();
            return out.toByteArray();
        } catch (IOException er) {
            LOGGER.error("Unable to serialize block", er);
            LOGGER.debug("Mensagem: "+ block.getRawBlock());
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Block.class.isAssignableFrom(clazz);
    }
}
