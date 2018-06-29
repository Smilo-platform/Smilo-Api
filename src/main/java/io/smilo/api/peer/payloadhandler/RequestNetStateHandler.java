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

package io.smilo.api.peer.payloadhandler;

import io.smilo.api.block.BlockStore;
import io.smilo.api.block.data.transaction.Transaction;
import io.smilo.api.peer.Peer;
import io.smilo.api.pendingpool.PendingBlockDataPool;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestNetStateHandler implements PayloadHandler {

    private final static Logger LOGGER = Logger.getLogger(RequestNetStateHandler.class);

    private BlockStore blockStore;
    private PendingBlockDataPool pendingBlockDataPool;

    public RequestNetStateHandler(BlockStore blockStore, PendingBlockDataPool pendingBlockDataPool) {
        this.blockStore = blockStore;
        this.pendingBlockDataPool = pendingBlockDataPool;
    }

    @Override
    public void handlePeerPayload(List<String> parts, Peer peer) {
        Integer networkHeight = blockStore.getLatestBlockHeight() + 1;
        LOGGER.debug("Data: NETWORK_STATE, BlockchainLength: " + networkHeight + ", LatestBlock: " + blockStore.getLatestBlockHash());
        peer.write("NETWORK_STATE " + networkHeight + " " + blockStore.getLatestBlockHash());
        pendingBlockDataPool.getPendingData(Transaction.class).stream()
                .forEach(t -> {
                    peer.write("TRANSACTION " + t);
                });
    }

    @Override
    public PayloadType supports() {
        return PayloadType.REQUEST_NET_STATE;
    }
}
