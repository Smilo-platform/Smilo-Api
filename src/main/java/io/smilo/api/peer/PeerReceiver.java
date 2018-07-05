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
package io.smilo.api.peer;

import io.smilo.api.block.*;
import io.smilo.api.peer.payloadhandler.PayloadHandlerProvider;
import io.smilo.api.peer.payloadhandler.PayloadType;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PeerReceiver {

    private static final Logger LOGGER = Logger.getLogger(PeerReceiver.class);
    
    private final BlockStore blockStore;
    private final PeerClient peerClient;
    private final PayloadHandlerProvider payloadHandlerProvider;
    private final NetworkState networkState;

    public PeerReceiver(BlockStore blockStore,
                        PeerClient peerClient,
                        PayloadHandlerProvider payloadHandlerProvider, NetworkState networkState) {
        this.blockStore = blockStore;
        this.peerClient = peerClient;
        this.payloadHandlerProvider = payloadHandlerProvider;
        this.networkState = networkState;
    }

    /**
     * Retrieve and handle new data from peers
     */
    public void getNewData() {
        LOGGER.trace("Look for new data from peers...");
        LOGGER.trace("Connected with " + peerClient.getPeers().size() + " threads...");

        // copy the peer list to make sure we don't get any concurrency issues when adding new received peers
        new ArrayList<>(peerClient.getPeers()).stream().filter(p -> p.isInitialized()).forEach(peer -> {
            List<String> input = peer.readData();
            if (input == null) {
                LOGGER.error("NULL RET RETRY");
                System.exit(-4);
            }
            /*
             * While taking up new transactions and blocks, the client will broadcast them to the network if they are new to the client.
             * As a result, if you are connected to 7 peers, you will get reverb 7 times for a broadcast of a block or transaction.
             * For now, this is done to MAKE SURE everyone is on the same page with block/transaction propagation.
             * In the future, much smarter algorithms for routing, perhaps sending "have you seen xxx transaction" or similar will be used.
             * No point in sending 4 KB when a 64-byte message (or less) could check to make sure a transaction hasn't already been sent.
             * Not wanting to complicate Proof of Concept, there are no fancy algorithms or means of telling if peers have already heard the news you are going to deliver.
             */
            input.forEach(data -> {
                if (data.length() > 60) {
                    LOGGER.info("got data: " + data.substring(0, 30) + "..." + data.substring(data.length() - 30, data.length()));
                } else {
                    LOGGER.info("got data: " + data);
                }
                List<String> parts = Arrays.asList(data.split(" "));
                if (!parts.isEmpty()) {
                    handlePayload(parts, peer);
                }
            });
        });
    }

    /**
     * Broadcast a new block request if catchupMode is true
     */
    public void broadcastNewBlockRequest() {
        if(networkState.getCachupMode()) {
            try {
                //Sleep for a bit, wait for responses before requesting more data.
                Thread.sleep(300);
                //Broadcast request for new block(s)
                Long blockNum =  blockStore.getLatestBlockHeight() + 1;
                LOGGER.info("Requesting block " + blockNum + "...");
                peerClient.broadcast("GET_BLOCK " + blockNum);
            } catch (InterruptedException e) {
                //If this throws an error, something's terribly off.
                LOGGER.error("P2pNetwork has mental illness.");
            }
        }
    }

    /**
     * Broadcast a new block request if catchupMode is true
     */
    public void broadcastNewNetStateRequest() {
        if(networkState.getCachupMode() && (blockStore.getLatestBlockHeight() > networkState.getTopBlock())) {
            peerClient.broadcast("REQUEST_NET_STATE");
        }
    }

    private void handlePayload(List<String> parts, Peer peer) {
        try {
            PayloadType type = PayloadType.valueOf(StringUtils.upperCase(parts.get(0)));
            payloadHandlerProvider.getPayloadHandler(type).handlePeerPayload(parts, peer);
        } catch (IllegalArgumentException e) {
            LOGGER.info("Unknown payload: " + StringUtils.upperCase(parts.get(0)) + ", do nothing.");
        }
    }

    /**
     * Update the data from the peer network
     */
    public void run() {
        broadcastNewNetStateRequest();
        getNewData();
        broadcastNewBlockRequest();
    }

}