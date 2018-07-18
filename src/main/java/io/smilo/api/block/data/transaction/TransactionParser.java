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
import io.smilo.api.address.AddressUtility;
import io.smilo.api.block.data.BlockDataParser;
import io.smilo.api.block.data.Parser;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.ByteArrayOutputStream;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessagePacker;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.core.buffer.ArrayBufferInput;
import org.msgpack.core.buffer.MessageBufferInput;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionUtility simplifies a few basic tasks dealing with transaction parsing and verification.
 */
@Component
public class TransactionParser extends BlockDataParser implements Parser<Transaction> {

    private static final Logger LOGGER = Logger.getLogger(TransactionParser.class);
    private final AddressUtility addressUtility;
    private final ObjectMapper dataMapper;


    public TransactionParser(AddressUtility addressUtility,
                             ObjectMapper dataMapper) {
        this.addressUtility = addressUtility;
        this.dataMapper = dataMapper;
    }

    /**
     * Tests whether a transaction is valid. Doesn't test account balances, but tests formatting and signature verification.
     *
     * @param transaction Transaction String to test: InputAddress;InputAmount;OutputAddress1;OutputAmount1;OutputAddress2;OutputAmount2...;SignatureData;SignatureIndex
     * @return boolean Whether the transaction is formatted and signed correctly
     */
    @Override
    public boolean isValid(Transaction transaction) {
        boolean isValid = true;
        try {
            if(transaction.getDataHash().equals("")){
                LOGGER.error("Error validating Tx hash: " + transaction.getDataHash() + " not valid.");
                isValid = false;
            }

            // Todo: Fix this
//            if (!transaction.getDataHash().equals(generateDataHash(transaction.getHashableData().getBytes()))){
//                LOGGER.error("Error validating Tx hash: " + transaction.getDataHash() + " not valid.");
//                isValid = false;
//            }

            if (!addressUtility.isAddressFormattedCorrectly(transaction.getInputAddress())) {
                LOGGER.error("Error validating transaction: input address " + transaction.getInputAddress() + " is misformatted.");
                isValid = false;
            }

            for (TransactionOutput output : transaction.getTransactionOutputs()) {
                if (!addressUtility.isAddressFormattedCorrectly(output.getOutputAddress())) {
                    LOGGER.error("Error validating transaction: output address " + output.getOutputAddress() + " is misformatted.");
                    isValid = false;
                }
            }

            if (transaction.getInputAmount() - transaction.getOutputTotal() < 0) {
                LOGGER.debug("Input amount: " + transaction.getInputAmount() + " & Output amount: " + transaction.getOutputTotal());
                LOGGER.error("Input amount is smaller then output amount!");
                isValid = false;
                // Coins can't be created out of thin air!
            }

            if (transaction.getInputAmount() - transaction.getOutputTotal() > 0) {
                LOGGER.debug("Input amount: " + transaction.getInputAmount() + " & Output amount: " + transaction.getOutputTotal());
                LOGGER.error("Input amount is bigger then output amount!");
                return false; //Where do they need to go? We don't have greedy miners.
            }

            // Todo:
            // Get inputaddress balance
            // Get outputaddress balance
            // Check if inputAmount <= inputAddressBalance

        } catch (Exception e) {
            // Likely an error parsing a Long or performing some String manipulation task. Maybe array bounds exceptions.
            LOGGER.error("Exception when validating transaction ", e);
            isValid = false;

        }
        LOGGER.info("Tx hash: " + transaction.getDataHash() + " is valid.");
        return isValid;
    }

    @Override
    public Transaction deserialize(byte[] raw) {
        if (raw.length == 0) return null;
        MessageBufferInput data = new ArrayBufferInput(raw);
        MessageUnpacker msgpack = MessagePack.newDefaultUnpacker(data);
        Transaction transaction = null;
        try {
            Long timestamp = msgpack.unpackLong();
            String assetId = msgpack.unpackString();
            String inputAddress = msgpack.unpackString();
            Long inputAmount = msgpack.unpackLong();
            int items = msgpack.unpackArrayHeader();
            List<TransactionOutput> outputs = new ArrayList<>();
            for (int i = 0; i < items; i++) {
                int size = msgpack.unpackBinaryHeader();
                byte[] temp = msgpack.readPayload(size);
                outputs.add(parseOutput(temp));
            }
            Long fee = msgpack.unpackLong();
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
        Long outputAmount = msgpack.unpackLong();
        return new TransactionOutput(outputAddress,outputAmount);
    }

    @Override
    public byte[] serialize(Transaction transaction) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (MessagePacker msgpack = MessagePack.newDefaultPacker(out)) {
            msgpack.packLong(transaction.getTimestamp());
            msgpack.packString(transaction.getAssetId());
            msgpack.packString(transaction.getInputAddress());
            msgpack.packLong(transaction.getInputAmount());
            List<TransactionOutput> outputs = transaction.getTransactionOutputs();
            msgpack.packArrayHeader(outputs.size());
            for (TransactionOutput txout : outputs) {
                byte[] rawoutput = packOutput(txout);
                msgpack.packBinaryHeader(rawoutput.length);
                msgpack.addPayload(rawoutput);
            }
            msgpack.packLong(transaction.getFee());
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
            msgpack.packLong(txout.getOutputAmount());
            msgpack.flush();
        }
        return out.toByteArray();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Transaction.class.isAssignableFrom(clazz);
    }

}
