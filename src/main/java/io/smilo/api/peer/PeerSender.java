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

import org.springframework.stereotype.Component;

@Component
public class PeerSender {

    private final PeerClient peerClient;

    public PeerSender(PeerClient peerClient) {
        this.peerClient = peerClient;
    }

    //TODO: fix type
    public void broadcast(String type, String data) {
        peerClient.broadcast(type + " " + data);
    }
}