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

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.net.ServerSocket;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Component
public class PeerClient {

    private static final Logger LOGGER = Logger.getLogger(PeerClient.class);
    private static final int DEFAULT_PORT = 8020;

    private final List<String> nodes;

    private final int listenPort;
    private boolean shouldRun = true;

    // TODO: check if it's random enough
    private final Random random = new Random();
    private PeerInitializer peerInitializer;
    private PeerStore peerStore;

    public PeerClient(@Value("#{'${NODES_LIST}'.split(',')}") List<String> nodes,
                      TaskExecutor taskExecutor,
                      PeerInitializer peerInitializer,
                      PeerStore peerStore) {
        this.peerInitializer = peerInitializer;
        this.peerStore = peerStore;
        this.listenPort = DEFAULT_PORT;
        this.nodes = nodes;
        initializePeers();
        listenToSocket(taskExecutor);
    }

    /**
     * Pauses listening to the peer network
     */
    public void pauseListening() {
        this.shouldRun = false;
    }

    /**
     * Resumes listening to the peer network
     */
    public void resumeListening() {
        this.shouldRun = true;
    }

    void initializePeers() {
        if (peerStore.getPeers().isEmpty()) {
            LOGGER.info("PeerStore does not exist.. Creating...");
            /*
             * In future networks, these will route to servers running the daemon. For now, it's just the above nodes.
             */
            nodes.stream()
                    .map(node -> {
                        String host = node.split(":")[0];
                        int port = Integer.parseInt(node.split(":")[1]);
                        return peerInitializer.initializePeer(host, port);
                    })
                    .filter(Objects::nonNull)
                    .forEach(this::connectToPeer);
        }
    }

    private void listenToSocket(TaskExecutor taskExecutor) {
        taskExecutor.execute(() -> {
            try (ServerSocket listenSocket = new ServerSocket(listenPort)) {
                while (shouldRun) //Doesn't actually quit right when shouldRun is changed, as while loop is pending.
                {
                    Peer peer = peerInitializer.initializePeer(listenSocket.accept());
                    peerStore.save(peer);
                    peer.run();
                }

            } catch(java.net.BindException e) {
                LOGGER.error("Port " + listenPort + " already in use", e);
                System.exit(-1);
            } catch (Exception e) {
                LOGGER.error(e);
            }
        });
    }

    /**
     * Attempts a connection to an external peer
     *
     * @param peer Peer to connect to
     */
    public void connectToPeer(Peer peer) {
        try {
            peerStore.save(peer);
            peer.run();
        } catch (Exception e) {
            LOGGER.warn("Unable to connect to " + peer.getRemoteHost() + ":" + peer.getRemotePort());
        }
    }

    /**
     * Announces the same message to all peers simultaneously. Useful when re-broadcasting messages.
     *
     * @param toBroadcast String to broadcast to peers
     */
    public void broadcast(String toBroadcast) {
        peerStore.getPeers().forEach(peer -> {
            LOGGER.info("Sent:: " + toBroadcast);
            peer.write(toBroadcast);
        });
    }

    /**
     * Announces the same message to all peers except the ignored one simultaneously. Useful when re-broadcasting messages. Peer ignored as it's the peer that sent you info.
     *
     * @param toBroadcast String to broadcast to peers
     * @param peerToIgnore Peer to not send broadcast too--usually the peer who sent information that is being rebroadcast
     */
    public void broadcastIgnorePeer(String toBroadcast, Peer peerToIgnore) {
        peerStore.getPeers().stream()
                .filter(p -> !p.getIdentifier().equals(peerToIgnore.getIdentifier()))
                .forEach(peer -> {
                    LOGGER.info("Sent:: " + toBroadcast);
                    peer.write(toBroadcast);
                });
    }

    /*
     * Returns a random peer host/port combo to the querying peer.
     * Future versions will detect dynamic ports and not send peers likely to not support direct connections.
     * While not part of GET_PEER, very-far-in-the-future-versions may support TCP punchthrough assists.
     */
    public Peer getRandomPeer() {
        List<Peer> peers = peerStore.getPeers();
        return peers.get(random.nextInt(peers.size()));
    }

    public List<Peer> getPeers() {
        return peerStore.getPeers();
    }

}
