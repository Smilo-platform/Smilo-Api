package io.smilo.api.rest.service;

import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.peer.PeerClient;
import io.smilo.api.peer.payloadhandler.PayloadType;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("transactionService")
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    PeerClient peerClient;

    @Autowired
    PendingBlockDataPool dataPool;

    @Override
    public Transaction putTransaction(Transaction transaction) {
        // Do we use this method to broadcast to peers?
        // More importantly: how do we format the transaction?
        dataPool.addBlockData(transaction);

        return transaction;
    }
}
