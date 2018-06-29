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
 */

package io.smilo.core.peer.payloadhandler;

import io.smilo.core.peer.NetworkState;
import io.smilo.core.peer.Peer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NetworkStateHandler implements PayloadHandler {

    private final NetworkState networkState;

    public NetworkStateHandler(NetworkState networkState) {
        this.networkState = networkState;
    }

    @Override
    public void handlePeerPayload(List<String> parts, Peer peer) {
        networkState.setTopBlock(Integer.parseInt(parts.get(1)));
    }

    @Override
    public PayloadType supports() {
        return PayloadType.NETWORK_STATE;
    }
}
