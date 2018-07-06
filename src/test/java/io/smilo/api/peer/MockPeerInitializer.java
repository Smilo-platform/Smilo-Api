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

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.Socket;

@Component
@Profile("test")
public class MockPeerInitializer implements PeerInitializer {

    @Override
    public Peer initializePeer(String hostname, int port) {
        return new MockPeer(hostname, port);
    }

    @Override
    public Peer initializePeer(Socket socket) {
        String remoteHost = socket.getInetAddress() + "";
        remoteHost = remoteHost.split("/")[0];
        remoteHost = remoteHost.replace("\\", "");
        int remotePort = socket.getPort();

        return new MockPeer(remoteHost, remotePort);
    }
}
