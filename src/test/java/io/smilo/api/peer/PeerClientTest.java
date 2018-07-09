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

import io.smilo.api.AbstractSpringTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static junit.framework.TestCase.assertTrue;

public class PeerClientTest extends AbstractSpringTest {

    @Autowired
    private PeerClient peerClient;

    @Autowired
    private PeerBuilder peerBuilder;

    @Autowired
    public PeerStore peerStore;

    @Value("#{'${NODES_LIST}'.split(',')}")
    private List<String> nodes;

    @Test
    public void testConnectToPeer() throws InterruptedException {
        Peer peer = peerBuilder.peer_uninitialized().construct();
        peerClient.connectToPeer(peer);
        // This test is to fast.. So sleep 100 ms.
        Thread.sleep(100);
        assertTrue(peer.isInitialized());
    }

    @Test
    public void testInitialize() {
        List<Peer> peers = peerClient.getPeers();
        nodes.forEach(node -> {
            assertTrue(peers.stream().map(p -> p.getRemoteHost() + ":" + p.getRemotePort()).anyMatch(p -> p.equals(node)));
        });
    }

    @Test
    public void testBroadcast() {
        Peer peer = peerBuilder.peer_ready().save();
        peerClient.broadcast("REQUEST_NET_STATE");
        assertTrue(((MockPeer) peer).getWrittenData().get(1).startsWith("REQUEST_NET_STATE"));
    }

    @Test
    public void testGetRandomPeer() {
        List<Peer> peers = peerClient.getPeers();
        Peer randomPeer = peerClient.getRandomPeer();

        assertTrue(peers.contains(randomPeer));
    }
}
