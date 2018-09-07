package io.smilo.api.block.data.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smilo.commons.block.data.transaction.Transaction;
import io.smilo.commons.db.Store;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionAddressStore {
    private static final Logger LOGGER = Logger.getLogger(TransactionAddressStore.class);
    private static final String COLLECTION_NAME = "transactionaddress";

    private final Store store;
    private final ObjectMapper dataMapper;
    private final TransactionStore transactionStore;

    public TransactionAddressStore(Store store, ObjectMapper dataMapper, TransactionStore transactionStore) {
        this.store = store;
        this.dataMapper = dataMapper;
        this.transactionStore = transactionStore;

        store.initializeCollection(COLLECTION_NAME);
    }

    public void writeTransactionForAddress(Transaction transaction, String address) {
        final ByteBuffer valueBuffer;

        // Try and convert the transaction to a byte key buffer and value key buffer.
        try {
            byte[] valueBytes = transaction.getDataHash().getBytes(StandardCharsets.UTF_8);

            valueBuffer = ByteBuffer.allocateDirect(valueBytes.length);

            valueBuffer.put(valueBytes).flip();
        }
        catch(Exception ex) {
            LOGGER.error("Unable to convert transaction to byte array " + ex);
            return;
        }

        // Write to LMDB
        store.addToArray(COLLECTION_NAME, address, valueBuffer);
    }

    public long getTransactionCountForAddress(String address) {
        return store.getArrayLength(COLLECTION_NAME, address);
    }

    public List<TransactionDTO> getTransactionsForAddress(String address, long skip, long take, boolean isDescending) {
        // Get the raw transaction hash values
        List<byte[]> values = store.getArray(COLLECTION_NAME, address, skip, take, isDescending);

        List<TransactionDTO> transactions = new ArrayList<>();

        // For each raw transaction hash retrieve the transaction
        for(byte[] transactionHashBytes : values) {
            transactions.add(transactionStore.getTransactionByID(new String(transactionHashBytes, StandardCharsets.UTF_8)));
        }

        return transactions;
    }
}
