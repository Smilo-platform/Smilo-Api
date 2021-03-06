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
import io.smilo.commons.block.Block;
import io.smilo.commons.block.data.transaction.Transaction;
import io.smilo.commons.db.Store;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionStore {
    private static final Logger LOGGER = Logger.getLogger(TransactionStore.class);
    private static final String SORTED_COLLECTION_NAME = "transaction-hash-sorted";
    private static final String TRANSACTION_TABLE = "transaction";

    private final Store store;
    private final ObjectMapper dataMapper;

    public TransactionStore(Store store, ObjectMapper dataMapper) {
        this.store = store;
        this.dataMapper = dataMapper;

        store.initializeCollection(SORTED_COLLECTION_NAME);
        store.initializeCollection(TRANSACTION_TABLE);
    }

    /**
     * Writes the given Transaction to LMDB.
     *
     * @param transaction
     */
    public void writeTransactionToFile(Transaction transaction, Block block) {
        writeToNonSortedDatabase(transaction, block);
        writeHashToSortedDatabase(transaction);
    }

    /**
     * Writes the given transaction into the non-sorted database. This database can be used
     * to retrieve transaction objects based on the data hash property.
     *
     * @param transaction
     */
    private void writeToNonSortedDatabase(Transaction transaction, Block block) {

        // Try and convert the transaction to a byte key buffer and value key buffer.
        try {
            TransactionDTO dto = TransactionDTO.toDTO(transaction);
            dto.setBlockIndex(block.getBlockNum());
            byte[] keyBytes = dto.getDataHash().getBytes();
            byte[] valueBytes = dataMapper.writeValueAsBytes(dto);

            // Write to non-sorted database
            store.put(TRANSACTION_TABLE, keyBytes, valueBytes);
        } catch (Exception ex) {
            LOGGER.error("Unable to convert transaction to byte array " + ex);
            return;
        }


    }

    /**
     * Writes the data hash of the given transaction into the sorted database. This database can be used
     * to retrieve a list of transactions sorted on timestamp.
     *
     * @param transaction
     */
    private void writeHashToSortedDatabase(Transaction transaction) {

        // Try and convert the transaction to a byte key buffer and value key buffer.
        try {
            TransactionDTO dto = TransactionDTO.toDTO(transaction);
            byte[] sortedKeyBytes = (dto.getTimestamp().toString() + dto.getDataHash()).getBytes();
            byte[] valueBytes = transaction.getDataHash().getBytes();

            // Write to sorted database
            store.put(SORTED_COLLECTION_NAME, sortedKeyBytes, valueBytes);
        } catch (Exception ex) {
            LOGGER.error("Unable to convert transaction hash to byte array " + ex);
            return;
        }


    }

    /**
     * Returns the amount of transactions available in the database.
     *
     * @return
     */
    public long getTransactionCount() {
        return store.getEntries(TRANSACTION_TABLE);
    }

    public List<TransactionDTO> getTransactions(long skip, long take, boolean isDescending) {
        // Clamp take between 0 and 32
        take = Math.min(Math.max(take, 0), 32);

        List<byte[]> values = store.getAllAPI(SORTED_COLLECTION_NAME, skip, take, isDescending);

        // Convert byte[] to strings
        List<String> transactionHashes = new ArrayList<>();
        for (byte[] bytes : values) {
            transactionHashes.add(new String(bytes, StandardCharsets.UTF_8));
        }

        // Retrieve transactions
        List<TransactionDTO> transactions = new ArrayList<>();
        for (String dataHash : transactionHashes) {
            transactions.add(getTransactionByID(dataHash));
        }

        return transactions;
    }

    /**
     * Reads the Transaction with the given id from LMDB.
     *
     * @param id The id of the Transaction. This is equal to the data hash.
     * @return
     */
    public TransactionDTO getTransactionByID(String id) {
        byte[] idBytes = id.getBytes();
        byte[] rawTransaction = store.get(TRANSACTION_TABLE, idBytes);
        if (rawTransaction == null) {
            return null;
        }
        TransactionDTO dto;
        try {
            dto = dataMapper.readValue(rawTransaction, TransactionDTO.class);
        } catch (IOException ex) {
            LOGGER.error("Unable getTransactionByID " + id, ex);

            return null;
        }

        return dto;
    }
}
