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

import io.smilo.api.block.Content;
import io.smilo.api.block.ParserProvider;
import io.smilo.api.block.data.BlockDataParser;
import io.smilo.api.block.data.Parser;
import io.smilo.api.peer.payloadhandler.PayloadType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PeerSender {

    private final PeerClient peerClient;
    private final ParserProvider parserProvider;

    public PeerSender(PeerClient peerClient, ParserProvider parserProvider) {
        this.peerClient = peerClient;
        this.parserProvider = parserProvider;
    }

    //TODO: fix type
    public void broadcast(PayloadType type, String data) {
        peerClient.broadcast(type.name() + " " + data);
    }

    public void broadcastContent(PayloadType type, Content content) {
        Parser parser = parserProvider.getParser(content.getClass());
        String data = BlockDataParser.encode(parser.serialize(content));
        broadcast(type, data);
    }

    public void broadcastContent(Content content) {
        broadcastContent(PayloadType.valueOf(StringUtils.upperCase(content.getClass().getSimpleName())), content);
    }
}