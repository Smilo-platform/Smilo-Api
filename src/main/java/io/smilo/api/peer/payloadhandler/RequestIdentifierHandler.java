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
package io.smilo.api.peer.payloadhandler;

import io.smilo.api.peer.Peer;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RequestIdentifierHandler implements PayloadHandler {

    @Override
    public void handlePeerPayload(List<String> parts, Peer peer) {
        peer.write(PayloadType.RESPOND_IDENTIFIER.name() + " SMILOAPI");
    }

    @Override
    public PayloadType supports() {
        return PayloadType.REQUEST_IDENTIFIER;
    }
}
