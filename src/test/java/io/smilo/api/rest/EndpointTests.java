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

package io.smilo.api.rest;

import io.smilo.api.AbstractWebSpringTest;
import io.smilo.api.StableTests;
import io.smilo.api.TestUtility;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@Category(StableTests.class)
public class EndpointTests extends AbstractWebSpringTest {

    @Autowired
    private TestUtility testUtility;

    @Test
    public void shouldReturn200WhenSendingRequestToStatusController() throws Exception {

        this.webClient.perform(MockMvcRequestBuilders.get("/status"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void shouldReturn200WhenSendingRequestToTxController() throws Exception {

        this.webClient.perform(MockMvcRequestBuilders.get("/tx"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void shouldReturn200WhenRequestTxToTxController() throws Exception {
        testUtility.addBlockZero();
        this.webClient.perform(MockMvcRequestBuilders.get("/tx/7E6F401FFFD55A1AF05E666D7CF1CD5B38628D3E6FC0CFB9935E82D9BA5329B3"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void shouldReturn200WhenSendingRequestToManagementEndpoint() throws Exception {

        this.webClient.perform(MockMvcRequestBuilders.get("/actuator/info"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}