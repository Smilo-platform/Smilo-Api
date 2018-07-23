package io.smilo.api.rest.service;

import io.smilo.api.address.Address;
import io.smilo.api.address.AddressStore;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionAddressStore;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import io.smilo.api.rest.models.TransactionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("addressService")
public class AddressServiceImpl implements AddressService {
    @Autowired
    AddressStore addressStore;

    @Autowired
    TransactionAddressStore transactionAddressStore;

    @Autowired
    PendingBlockDataPool pendingBlockDataPool;

    @Override
    public Address getAddress(String address) {
        return addressStore.getByAddress(address);
    }

    @Override
    public TransactionList getTransactionsForAddress(String address, long skip, long take, boolean isDescending) {
        long count = transactionAddressStore.getTransactionCountForAddress(address);

        List<Transaction> transactions = transactionAddressStore.getTransactionsForAddress(address, skip, take, isDescending);

        // Append pending transactions
        List<Transaction> pendingTransactions = pendingBlockDataPool.getPendingTransactionsForAddress(address);
        transactions.addAll(pendingTransactions);

        return new TransactionList(transactions, skip, take, count + pendingTransactions.size());
    }
}
