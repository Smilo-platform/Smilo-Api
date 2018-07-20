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

import io.smilo.api.AbstractSpringTest;
import io.smilo.api.StableTests;
import io.smilo.api.peer.payloadhandler.PayloadHandler;
import io.smilo.api.peer.payloadhandler.PayloadHandlerProvider;
import io.smilo.api.peer.payloadhandler.PayloadType;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.TestCase.assertTrue;

@Category({StableTests.class})
public class PayLoadHandlerTest extends AbstractSpringTest {

    @Autowired
    private PayloadHandlerProvider payloadHandler;


    @Test
    public void testPayloadHandlerCOMMIT() {
        PayloadType type = PayloadType.valueOf(StringUtils.upperCase("COMMIT"));
        PayloadHandler result = payloadHandler.getPayloadHandler(type);

        assertTrue(result.supports().name().equals("COMMIT"));
    }

    @Test
    public void testPayloadHandlerTRANSACTION() {
        PayloadType type = PayloadType.valueOf(StringUtils.upperCase("TRANSACTION"));
        PayloadHandler result = payloadHandler.getPayloadHandler(type);

        assertTrue(result.supports().name().equals("TRANSACTION"));
    }

    @Test
    public void testPayloadHandlerNETWORK_STATE() {
        PayloadType type = PayloadType.valueOf(StringUtils.upperCase("NETWORK_STATE"));
        PayloadHandler result = payloadHandler.getPayloadHandler(type);

        assertTrue(result.supports().name().equals("NETWORK_STATE"));
    }

    @Test
    public void testPayloadHandlerREQUEST_NET_STATE() {
        PayloadType type = PayloadType.valueOf(StringUtils.upperCase("REQUEST_NET_STATE"));
        PayloadHandler result = payloadHandler.getPayloadHandler(type);

        assertTrue(result.supports().name().equals("REQUEST_NET_STATE"));
    }

    @Test
    public void testPayloadHandlerREQUEST_IDENTIFIER() {
        PayloadType type = PayloadType.valueOf(StringUtils.upperCase("REQUEST_IDENTIFIER"));
        PayloadHandler result = payloadHandler.getPayloadHandler(type);

        assertTrue(result.supports().name().equals("REQUEST_IDENTIFIER"));
    }

    @Test
    public void testPayloadHandlerRESPOND_IDENTIFIER() {
        boolean result = false;
        try {
            PayloadType type = PayloadType.valueOf(StringUtils.upperCase("RESPOND_IDENTIFIER"));
            PayloadHandler handler = payloadHandler.getPayloadHandler(type);
        } catch (IllegalArgumentException e){
            result = true;
        }
        assertTrue(result);
    }

    @Test
    public void testPayloadHandlerBLOCK() {
        PayloadType type = PayloadType.valueOf(StringUtils.upperCase("BLOCK"));
        PayloadHandler result = payloadHandler.getPayloadHandler(type);

        assertTrue(result.supports().name().equals("BLOCK"));
    }

    @Test
    public void testPayloadHandlerEMPTY() {
        boolean result = false;
        try {
            PayloadType type = PayloadType.valueOf(StringUtils.upperCase(""));
        } catch (IllegalArgumentException e){
            result = true;
        }
        assertTrue(result);
    }

    @Test
    public void testPayloadHandlerUNKNOWN() {
        boolean result = false;
        try {
            PayloadType type = PayloadType.valueOf(StringUtils.upperCase("ljjhdfkjlhdjkfhjk"));
        } catch (IllegalArgumentException e){
            result = true;
        }
        assertTrue(result);
    }
}


