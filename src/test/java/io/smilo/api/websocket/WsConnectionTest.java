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

package io.smilo.api.websocket;

import io.smilo.api.Application;
import io.smilo.api.StableTests;
import io.smilo.api.TestConfig;
import io.smilo.api.TestUtility;
import io.smilo.api.block.data.BlockDataParser;
import io.smilo.api.block.data.transaction.TransactionBuilder;
import io.smilo.api.peer.MockPeer;
import io.smilo.api.peer.PeerBuilder;
import io.smilo.api.peer.payloadhandler.TransactionHandlerAPI;
import io.smilo.api.ws.Websocket;
import io.smilo.commons.block.data.transaction.Transaction;
import io.smilo.commons.block.data.transaction.TransactionParser;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

@FixMethodOrder(NAME_ASCENDING)
@SpringBootTest(classes = {Application.class, TestConfig.class}, webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "local"})
@Category(StableTests.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class WsConnectionTest {

    private static final Logger LOGGER = Logger.getLogger(WsConnectionTest.class);

    @Autowired
    private TestUtility testUtility;

    private WebsocketClientEndpoint clientEndPoint;

    @Autowired
    private PeerBuilder peerBuilder;

    @Autowired
    private TransactionBuilder transactionBuilder;

    @Autowired
    private TransactionHandlerAPI transactionHandlerAPI;

    @Autowired
    private TransactionParser transactionParser;

    @Value("${local.server.port}")
    public int port;

    @Before
    public void initWsConnectionTest() {
        testUtility.cleanUp();

        LOGGER.info("Connecting to: ws://localhost:" + port + "/websocket");
        try {
            // open websocket
            clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:"+port+"/websocket"));

            // wait 0.1 seconds for messages from websocket
            Thread.sleep(100);

        } catch (URISyntaxException ex) {
            LOGGER.error("URISyntaxException exception: " + ex.getMessage());
        } catch (InterruptedException ex) {
            LOGGER.error("InterruptedException exception: " + ex.getMessage());
        }
    }

    @Test()
    public void test1ConnectToWs() {
        assertEquals("Welcome to the Smilo Api!", clientEndPoint.lastMessage());
    }

    @Test
    public void test2BroadcastPendingTransactionOverWebSocket() {
        Transaction transaction = transactionBuilder.empty().construct();

        MockPeer peer = peerBuilder.peer_ready().save();
        List<String> parts = new ArrayList<>();
        parts.add("TRANSACTION");
        parts.add(BlockDataParser.encode(transactionParser.serialize(transaction)));
        transactionHandlerAPI.handlePeerPayload(parts, peer);

        JSONObject jsonObject = new Websocket().generateTxObject(transaction);

        JSONObject obj = new JSONObject();
        obj.remove("type");
        obj.remove("data");
        obj.put("data", jsonObject);
        obj.put("type", "PENDING_BLOCK_DATA");

        assertEquals(obj.toJSONString(), clientEndPoint.lastMessage());
    }
}
