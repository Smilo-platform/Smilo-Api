package io.smilo.api.rest.service;

import io.smilo.api.block.AddResultType;
import io.smilo.api.block.data.AddBlockDataResult;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.peer.PeerClient;
import io.smilo.api.peer.PeerSender;
import io.smilo.api.peer.payloadhandler.PayloadType;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import io.smilo.api.rest.models.PostTransactionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("transactionService")
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    PeerSender peerSender;

    @Autowired
    PendingBlockDataPool pendingBlockDataPool;

    @Override
    public PostTransactionResult putTransaction(Transaction transaction) {
        AddBlockDataResult result = pendingBlockDataPool.addBlockData(transaction);
        if(result.getType() == AddResultType.ADDED) {
            peerSender.broadcastContent(transaction);

            return new PostTransactionResult(true, "");
        }
        else {
            return new PostTransactionResult(false, result.getMessage());
        }
    }
}
