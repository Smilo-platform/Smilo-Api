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

import io.smilo.api.AbstractSpringTest;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import static junit.framework.TestCase.assertEquals;

public class WsConnectionTest extends AbstractSpringTest {

    private static final Logger LOGGER = Logger.getLogger(WsConnectionTest.class);

    @Test
    public void testConnectToWs() {
        try {
            // open websocket
            LOGGER.info("Connecting to: ws://localhost:" + port + "/websocket");
            final WebsocketClientEndpoint clientEndPoint = new WebsocketClientEndpoint(new URI("ws://localhost:"+port+"/websocket"));

            // wait 0.1 seconds for messages from websocket
            Thread.sleep(100);

            assertEquals("Welcome tot the Smilo Api!", clientEndPoint.lastMessage());

        } catch (InterruptedException ex) {
            LOGGER.error("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            LOGGER.error("URISyntaxException exception: " + ex.getMessage());
        }

    }

}
