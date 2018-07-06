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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PeerBuilder {

    @Autowired
    private PeerStore peerStore;
    @Autowired
    private PeerClient peerClient;

    public PeerBuildCommand blank() {
        return new PeerBuildCommand().blank("localhost", 80);
    }

    public PeerBuildCommand blank(String hostname, int port) {
        return new PeerBuildCommand().blank(hostname, port);
    }

    public PeerBuildCommand peer_ready() {
        return blank().withInitialized(true);
    }

    public PeerBuildCommand peer_uninitialized() {
        return blank().withInitialized(false);
    }

    public class PeerBuildCommand {

        private MockPeer peer;

        public PeerBuildCommand blank(String hostname, int port) {
            this.peer = new MockPeer(hostname, port);
            return this;
        }

        public PeerBuildCommand withInitialized(boolean initialized) {
            peer.setInitialized(initialized);
            return this;
        }

        public PeerBuildCommand withRemoteHost(String remoteHost) {
            peer.setRemoteHost(remoteHost);
            return this;
        }

        public PeerBuildCommand withRemotePort(int remotePort) {
            peer.setRemotePort(remotePort);
            return this;
        }

        public MockPeer save() {
            peerClient.connectToPeer(peer);
            peerStore.save(peer);
            return peer;
        }

        public MockPeer construct() {
            return peer;
        }
    }

}
