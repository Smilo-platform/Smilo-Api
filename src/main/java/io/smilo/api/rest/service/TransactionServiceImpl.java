package io.smilo.api.rest.service;

import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.peer.PeerClient;
import io.smilo.api.peer.PeerSender;
import io.smilo.api.peer.payloadhandler.PayloadType;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("transactionService")
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    PeerSender peerSender;

    @Override
    public Transaction putTransaction(Transaction transaction) {
        peerSender.broadcastContent(transaction);

        return transaction;
    }
}
