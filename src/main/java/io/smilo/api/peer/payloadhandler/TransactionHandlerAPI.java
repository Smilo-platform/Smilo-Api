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

import io.smilo.api.ws.Websocket;
import io.smilo.commons.block.data.BlockDataParser;
import io.smilo.commons.block.data.transaction.Transaction;
import io.smilo.commons.block.data.transaction.TransactionParser;
import io.smilo.commons.peer.IPeer;
import io.smilo.commons.peer.payloadhandler.PayloadHandler;
import io.smilo.commons.peer.payloadhandler.PayloadType;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionHandlerAPI implements PayloadHandler {

    private TransactionParser transactionParser;
    private Websocket websocket;

    private static final Logger LOGGER = Logger.getLogger(TransactionHandlerAPI.class);

    public TransactionHandlerAPI(Websocket websocket, TransactionParser transactionParser) {
        this.websocket = websocket;
        this.transactionParser = transactionParser;
    }

    @Override
    public void handlePeerPayload(List<String> parts, IPeer peer) {
        byte[] byteArray = BlockDataParser.decode(parts.get(1));
        Transaction transaction = transactionParser.deserialize(byteArray);
        LOGGER.info("Broadcasting to websocket: " + parts.get(1));
        websocket.sendBlockData(transaction);
    }

    @Override
    public PayloadType supports() {
        return PayloadType.TRANSACTION;
    }
}

