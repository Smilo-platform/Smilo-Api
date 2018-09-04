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
import io.smilo.api.ws.Websocket;
import io.smilo.commons.block.Block;
import io.smilo.commons.block.genesis.GenesisLoader;
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

import static junit.framework.Assert.assertNotNull;
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
    private GenesisLoader genesis;

    @Value("${local.server.port}")
    public int port;

    @Before
    public void initWsConnectionTest() {

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

    @Test()
    public void test2ShouldReceiveGenesisBlockBroadcast(){
        testUtility.initialize();

        LOGGER.info("Loading genesis block...");

        Block blockgenesis = genesis.loadGenesis();

        assertNotNull("Genesis block not loaded", blockgenesis);

        LOGGER.info("Genesis block loaded: " + blockgenesis);

        JSONObject jsonObject = new Websocket().generateBlockObject(blockgenesis);

        JSONObject obj = new JSONObject();
        obj.remove("type");
        obj.remove("data");
        obj.put("data", jsonObject);
        obj.put("type", "BLOCK");

        assertEquals(obj.toJSONString(), clientEndPoint.lastMessage());
    }
}
