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

package io.smilo.api.block.data.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smilo.api.address.AddressDTO;
import io.smilo.api.address.AddressStore;
import io.smilo.api.address.AddressUtility;
import io.smilo.api.block.data.BlockDataParser;
import io.smilo.api.block.data.Parser;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.ArrayBufferInput;
import org.msgpack.core.buffer.MessageBufferInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * TransactionUtility simplifies a few basic tasks dealing with transaction parsing and verification.
 */
@Component
public class TransactionParser extends BlockDataParser implements Parser<Transaction> {

    private static final Logger LOGGER = Logger.getLogger(TransactionParser.class);
    private final AddressUtility addressUtility;
    private final ObjectMapper dataMapper;
    private final AddressStore addressStore;
    private static final byte CURRENT_VERSION = (byte) 1;

    public TransactionParser(AddressUtility addressUtility,
                             ObjectMapper dataMapper,
                             AddressStore addressStore) {
        this.addressUtility = addressUtility;
        this.dataMapper = dataMapper;
        this.addressStore = addressStore;
    }

    public void hash(Transaction transaction) {
        try {
            transaction.setDataHash(generateDataHash(transaction.getRawTransactionData().getBytes()));
        } catch (Exception ex) {
            LOGGER.error("Unable to create data hash for transaction", ex);
        }
    }

    /**
     * Tests whether a transaction is valid. Doesn't test account balances, but tests formatting and signature verification.
     *
     * @param transaction Transaction String to test: InputAddress;InputAmount;OutputAddress1;OutputAmount1;OutputAddress2;OutputAmount2...;SignatureData;SignatureIndex
     * @return boolean Whether the transaction is formatted and signed correctly
     */
    @Override
    public boolean isValid(Transaction transaction) {
        try {
            if(transaction.getDataHash().equals("")){
                LOGGER.error("Error validating Tx hash: " + transaction.getDataHash() + " not valid.");
                return false;
            }

            if (transaction.getDataHash().equals(generateDataHash(transaction.getRawTransactionData().getBytes()))){
                LOGGER.info("Tx hash: " + transaction.getDataHash() + " is valid.");
            } else {
                LOGGER.error("Error validating Tx hash: " + transaction.getDataHash() + " not valid.");
                return false;
            }

            if (!addressUtility.isAddressFormattedCorrectly(transaction.getInputAddress())) {
                LOGGER.error("Error validating transaction: input address " + transaction.getInputAddress() + " is misformatted.");
                return false;
            }

            for (TransactionOutput output : transaction.getTransactionOutputs()) {
                if (!addressUtility.isAddressFormattedCorrectly(output.getOutputAddress())) {
                    LOGGER.error("Error validating transaction: output address " + output.getOutputAddress() + " is misformatted.");
                    return false;
                }
            }

            if (transaction.getInputAmount().compareTo(transaction.getOutputTotal()) < 0) {
                LOGGER.debug("Input amount: " + transaction.getInputAmount() + " & Output amount: " + transaction.getOutputTotal());
                LOGGER.error("Input amount is smaller then output amount!");
                // Coins can't be created out of thin air!
                return false;
            }

            if (transaction.getInputAmount().compareTo(transaction.getOutputTotal()) > 0) {
                LOGGER.debug("Input amount: " + transaction.getInputAmount() + " & Output amount: " + transaction.getOutputTotal());
                LOGGER.error("Input amount is bigger then output amount!");
                return false; //Where do they need to go? We don't have greedy miners.
            }

            if (!addressUtility.verifyMerkleSignature(transaction.getRawTransactionDataWithHash(), transaction.getSignatureData(), transaction.getInputAddress(), transaction.getSignatureIndex())) {
                LOGGER.error("Error validating block: Transaction signature does not match!");
                return false;
            }

            AddressDTO address = this.addressStore.getByAddress(transaction.getInputAddress());
            if(address == null) {
                LOGGER.error("Unknown address so it has no balance to spent");
                return false;
            }

            // Get the balance including any pending transactions
            BigInteger balance = address.getBalance(transaction.getAssetId());

            // Enable below lines in the future once we figure out how to get PendingBlockDataPool in this class without creating a circular dependency!
            // Get pending transactions for the address
//            List<Transaction> pendingTransactions = this.pendingBlockDataPool.getPendingTransactionsForAddress(address.getAddress());
//            for(Transaction pending : pendingTransactions) {
//                if(pending.getAssetId().equals(transaction.getAssetId())) {
//                    // This transaction influences the same asset as the current transaction
//                    if(pending.getInputAddress().equals(address.getAddress())) {
//                        // This address is already spending
//                        balance = balance.subtract(pending.getInputAmount());
//                    }
//                    else {
//                        // This address is receiving, find how much
//                        for(TransactionOutput pendingOutput : pending.getTransactionOutputs()) {
//                            if(pendingOutput.getOutputAddress().equals(address.getAddress())) {
//                                balance = balance.add(pendingOutput.getOutputAmount());
//                            }
//                        }
//                    }
//                }
//            }

            if(balance.compareTo(transaction.getOutputTotal()) < 0) {
                LOGGER.error("Spending too much");
                return false;
            }
        } catch (Exception e) {
            // Likely an error parsing a Long or performing some String manipulation task. Maybe array bounds exceptions.
            LOGGER.error("Exception when validating transaction ", e);
            return false;
        }

        return true;
    }

    @Override
    public Transaction deserialize(byte[] raw) {
        if (raw.length == 0) return null;
        MessageBufferInput data = new ArrayBufferInput(raw);
        MessageUnpacker msgpack = MessagePack.newDefaultUnpacker(data);
        Transaction transaction = null;
        try {
            msgpack.unpackByte(); // Skip version number
            Long timestamp = msgpack.unpackLong();
            String assetId = msgpack.unpackString();
            String inputAddress = msgpack.unpackString();
            BigInteger inputAmount = msgpack.unpackBigInteger();
            int items = msgpack.unpackArrayHeader();
            List<TransactionOutput> outputs = new ArrayList<>();
            for (int i = 0; i < items; i++) {
                int size = msgpack.unpackBinaryHeader();
                byte[] temp = msgpack.readPayload(size);
                outputs.add(parseOutput(temp));
            }
            BigInteger fee = msgpack.unpackBigInteger();
            int size = msgpack.unpackBinaryHeader();
            byte [] extraData = msgpack.readPayload(size);
            LOGGER.info("Unsupported extra data inside: " + Hex.encodeHexString(extraData));
            String hash = msgpack.unpackString();
            String signature = msgpack.unpackString();
            Long signatureIndex = msgpack.unpackLong();

            transaction = new Transaction(timestamp, assetId, inputAddress, inputAmount, fee, outputs, hash, signature, signatureIndex);
        } catch (ArrayIndexOutOfBoundsException | IOException ex) {
            LOGGER.error("Unable to deserialize transaction", ex);
        }
        return transaction;
    }

    private TransactionOutput parseOutput(byte[] raw) throws IOException {
        MessageBufferInput data = new ArrayBufferInput(raw);
        MessageUnpacker msgpack = MessagePack.newDefaultUnpacker(data);
        String outputAddress = msgpack.unpackString();
        BigInteger outputAmount = msgpack.unpackBigInteger();
        return new TransactionOutput(outputAddress,outputAmount);
    }

    @Override
    public byte[] serialize(Transaction transaction) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msgpack = MessagePack.newDefaultPacker(out)) {
            msgpack.packByte(CURRENT_VERSION);
            msgpack.packLong(transaction.getTimestamp());
            msgpack.packString(transaction.getAssetId());
            msgpack.packString(transaction.getInputAddress());
            msgpack.packBigInteger(transaction.getInputAmount());
            List<TransactionOutput> outputs = transaction.getTransactionOutputs();
            msgpack.packArrayHeader(outputs.size());
            for (TransactionOutput txout : outputs) {
                byte[] rawoutput = packOutput(txout);
                msgpack.packBinaryHeader(rawoutput.length);
                msgpack.addPayload(rawoutput);
            }
            msgpack.packBigInteger(transaction.getFee());
            msgpack.packBinaryHeader(0);
            msgpack.packString(transaction.getDataHash());
            msgpack.packString(transaction.getSignatureData());
            msgpack.packLong(transaction.getSignatureIndex());
            msgpack.flush();
            return out.toByteArray();
        } catch (ArrayIndexOutOfBoundsException | IOException ex) {
            LOGGER.error("Unable to serialize transaction", ex);
        }
        return null;
    }

    private byte[] packOutput(TransactionOutput txout) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msgpack = MessagePack.newDefaultPacker(out)) {
            msgpack.packString(txout.getOutputAddress());
            msgpack.packBigInteger(txout.getOutputAmount());
            msgpack.flush();
        }
        return out.toByteArray();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Transaction.class.isAssignableFrom(clazz);
    }

}
