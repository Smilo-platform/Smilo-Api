package io.smilo.api.rest.service;

import io.smilo.api.address.AddressDTO;
import io.smilo.api.address.AddressStore;
import io.smilo.api.block.data.transaction.TransactionAddressStore;
import io.smilo.api.block.data.transaction.TransactionDTO;
import io.smilo.api.rest.models.TransactionList;
import io.smilo.commons.ledger.Account;
import io.smilo.commons.ledger.LedgerStore;
import io.smilo.commons.pendingpool.PendingBlockDataPool;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("addressService")
public class AddressServiceImpl implements AddressService {
    private static final Logger LOGGER = Logger.getLogger(AddressServiceImpl.class);

    @Autowired
    AddressStore addressStore;

    @Autowired
    LedgerStore ledgerStore;

    @Autowired
    TransactionAddressStore transactionAddressStore;

    @Autowired
    PendingBlockDataPool pendingBlockDataPool;

    @Override
    public AddressDTO getAddress(String address) {
        Optional<Account> acc = ledgerStore.getByAddress(address);
        try {
            if (acc == null || !acc.isPresent() || acc.get().getBalance() == null) {
                LOGGER.debug("Account is empty ??? " + address);
            }
            return addressStore.getByAddress(address);
        }catch (Exception e){
            LOGGER.error(e);
        }
        return null;
    }

    @Override
    public TransactionList getTransactionsForAddress(String address, long skip, long take, boolean isDescending) {
        // The skip and take must take into account the existence of pending transactions (which are not found in the database).
        // Because of this we must first look at the pending transactions and based on the skip determine if they should be part
        // of the output. We must also look at the take and determine if we should even bother reading data from the database.
        // Finally we must take the isDescending into account and determine if the pending transactions should be added
        // before or after the database/confirmed transactions.

        // Limit take between 0 and 32
        take = Math.min(Math.max(take, 0), 32);

        // Retrieve pending transactions and convert to DTO.
        List<TransactionDTO> pendingTransactions = pendingBlockDataPool.getPendingTransactionsForAddress(address)
                .stream().map(x -> TransactionDTO.toDTO(x))
                .collect(Collectors.toList());

        // How much records to we got in the database?
        long count = transactionAddressStore.getTransactionCountForAddress(address);
        long pendingCount = pendingTransactions.size();

        // Pending transactions can only influence a call to the database if the order is set to descending.
        List<TransactionDTO> transactions = new ArrayList<>();
        if (isDescending) {
            // Do we need to make a database call?
            if (skip + take > pendingCount) {
                // Database call needed, adjust skip and take based on amount of pending transactions.
                transactions = transactionAddressStore.getTransactionsForAddress(
                        address,
                        skip, take - pendingCount,
                        isDescending
                );
            }
        } else {
            // If the order is ascending not all pending transactions might be added.
            transactions = transactionAddressStore.getTransactionsForAddress(address, skip, take, isDescending);

            long pendingTransactionsToTake = Math.max(take - transactions.size(), 0);
            pendingTransactions = pendingTransactions.subList(0, (int) Math.min(pendingTransactionsToTake, pendingCount));
        }

        // Sort pending transactions so they are in the correct order (based on timestamp).
        pendingTransactions.sort((x, y) -> {
            if (isDescending)
                return (int) (y.getTimestamp() - x.getTimestamp());
            else
                return (int) (x.getTimestamp() - y.getTimestamp());
        });

        // Merge database and pending together. If the order is descending the pending transactions will be added first.
        // If the order is ascending the pending transactions will be added last.
        if (isDescending) {
            pendingTransactions.addAll(transactions);

            // We assign pending transactions back to transactions
            // to make it easier in the return statement.
            transactions = pendingTransactions;
        } else
            transactions.addAll(pendingTransactions);

        return new TransactionList(transactions, skip, take, count + pendingCount);
    }
}
