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
import io.smilo.api.block.data.transaction.TransactionStore;
import io.smilo.api.peer.PeerClient;
import io.smilo.api.peer.PeerSender;
import io.smilo.api.peer.payloadhandler.PayloadType;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import io.smilo.api.rest.models.PostTransactionResult;
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
        return transactionStore.getTransaction(transactionHash);
    }

    @Override
    public List<Transaction> getAll() {
        return null;
    }

    @Override
    public List<Transaction> getAll(String address) {
        return null;
    }
}
