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

package io.smilo.api;

import io.smilo.api.block.BlockStore;
import io.smilo.api.peer.PeerClient;
import io.smilo.api.peer.PeerReceiver;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmiloApi {

    private static final Logger LOGGER = Logger.getLogger(SmiloApi.class);
    private final PeerReceiver peerReceiver;
    private final PeerClient peerClient;
    private final BlockStore blockStore;
    private final String version;
    public Boolean quit = false;

    public SmiloApi(@Value("${VERSION:prototype}") String version, PeerReceiver peerReceiver, PeerClient peerClient, BlockStore blockStore) {
        this.version = version;
        this.peerReceiver = peerReceiver;
        this.peerClient = peerClient;
        this.blockStore = blockStore;
    }

    /**
     * Runs the main loop of the Smilo Api.
     */
    public void run() {
        this.quit = false;
        LOGGER.info("Starting Smilo Platform Api " + version);

        blockStore.initialiseLatestBlock();

        // Todo: Implementation of Websocket & rest server for block explorer and wallets


        /*
          * Start the Api loop.
          * - Create Peer connection (as a client) to the Smilo nodes
          * - Receive blockHeight of nodes
          * - Retrieve missing blocks
          *     - All recieved blocks are valid blocks (APPROVED by the chain), no check is currently needed.
          * - Parse blocks into NoSQL DB (LMDB)
          *
         */
        while (true) {
            if (peerClient.getPeers().size() > 0){
                peerReceiver.run();
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            } else {
                // connect to more peers!!
                LOGGER.warn(peerClient.getPeers().size() + " connections are not enough! Try to reconnect to more!");
                peerReceiver.run();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    LOGGER.error(e);
                }
            }


            // Quit?
            if (quit){
                break;
            }
        }
    }

}
