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

import io.smilo.api.db.Store;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.nio.ByteBuffer.allocateDirect;
import static java.util.stream.Collectors.toList;

@Component
public class PeerStore {

    private static final String COLLECTION_NAME = "peer";

    private final Store store;
    private final PeerInitializer peerInitializer;

    private List<Peer> peers = new ArrayList<>();

    public PeerStore(Store store, PeerInitializer peerInitializer) {
        this.store = store;
        this.peerInitializer = peerInitializer;
        store.initializeCollection(COLLECTION_NAME);
    }

    public void save(Peer peer) {
            final ByteBuffer key = allocateDirect(64);
            final ByteBuffer val = allocateDirect(10000);

            // Identifier and raw peer are same, but raw peer will probably be expanded later
            key.put(peer.getIdentifier().getBytes(StandardCharsets.UTF_8)).flip();
            val.put(peer.getRawPeer().getBytes(StandardCharsets.UTF_8)).flip();
            store.put(COLLECTION_NAME, key, val);
            peers.add(peer);
    }

    // TODO: map using objectMapper
    public List<Peer> getPeers() {
        if (peers.isEmpty()) {
            return store.getAll(COLLECTION_NAME).values()
                    .stream()
                    .map(p -> peerInitializer.initializePeer(p.split(" ")[0], Integer.valueOf((p.split(" ")[1]))))
                    .filter(Objects::nonNull)
                    .collect(toList());
        } else {
            return peers;
        }
    }

    @PreDestroy
    public void clear() {
        store.clear(COLLECTION_NAME);
        peers.clear();
    }
}
