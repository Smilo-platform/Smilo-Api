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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SmiloApi {

    private static final Logger LOGGER = Logger.getLogger(SmiloApi.class);
    private final String version;

    public SmiloApi(@Value("${VERSION:prototype}") String version) {
        this.version = version;
    }

    /**
     * Runs the main loop of the Smilo Api.
     */
    public void run() {
        LOGGER.info("Starting Smilo Platform Api " + version);

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
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                LOGGER.error(e);
            }

        }
    }

}
