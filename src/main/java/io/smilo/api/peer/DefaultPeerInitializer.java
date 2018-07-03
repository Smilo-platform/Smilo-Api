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

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.Socket;

@Component
@Profile("default")
public class DefaultPeerInitializer implements PeerInitializer {

    private static final Logger LOGGER = Logger.getLogger(DefaultPeerInitializer.class);

    @Override
    public Peer initializePeer(String hostname, int port) {
        try {
            LOGGER.info("Connecting to " + hostname + ":" + port);
            return new Peer(hostname, port);
        } catch (IOException e) {
            LOGGER.error("Unable to connect to " + hostname + ":" + port);
            // TODO: remove peer from database?
            return null;
        }
    }

    @Override
    public Peer initializePeer(Socket socket) {
        return new Peer(socket);
    }


}
