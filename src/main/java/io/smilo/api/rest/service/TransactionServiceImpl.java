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

package io.smilo.api.rest.service;

import io.smilo.api.block.AddResultType;
import io.smilo.api.block.data.AddBlockDataResult;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.block.data.transaction.TransactionAddressStore;
import io.smilo.api.block.data.transaction.TransactionStore;
import io.smilo.api.peer.PeerClient;
import io.smilo.api.peer.PeerSender;
import io.smilo.api.peer.payloadhandler.PayloadType;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import io.smilo.api.rest.models.PostTransactionResult;
import io.smilo.api.rest.models.TransactionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("transactionService")
public class TransactionServiceImpl implements TransactionService {
    @Autowired
    PeerSender peerSender;

    @Autowired
    PendingBlockDataPool pendingBlockDataPool;

    @Autowired
    TransactionStore transactionStore;

    @Autowired
    TransactionAddressStore transactionAddressStore;

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

    @Override
    public Transaction get(String transactionHash) {
        Transaction transaction = transactionStore.getTransaction(transactionHash);

        if(transaction == null) {
            // Transaction could not be found in the database.
            // However it might still be in the pending block data pool.
            transaction = pendingBlockDataPool.getPendingTransaction(transactionHash);
        }

        return transaction;
    }

    @Override
    public TransactionList getAll(long skip, long take, boolean isDescending) {
        long count = transactionStore.getTransactionCount();

        List<Transaction> transactions = transactionStore.getTransactions(skip, take, isDescending);

        return new TransactionList(transactions, skip, take, count);
    }
}
