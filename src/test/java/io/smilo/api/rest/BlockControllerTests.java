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
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class BlockControllerTests extends AbstractWebSpringTest {

    @Test
    public void shouldReturn200WhenSendingRequestToRootBlockController() throws Exception {
        /**
         * /block returns latest block. This is equal to null
         */
        this.webClient.perform(MockMvcRequestBuilders.get("/block"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockNum").value(0));
    }

    @Test
    public void shouldReturn200WhenSendingRequestToBlockController() throws Exception {
        /**
         * /block returns latest block. This is equal to block 0
         */
        this.webClient.perform(MockMvcRequestBuilders.get("/block"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(1530261926))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockNum").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.previousBlockHash").value("0000000000000000000000000000000000000000000000000000000000000000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockHash").value("F96BD2EE2770B4CBD8BE49056DD1F88DC8F1CF462FAB142144214156EA0266AA"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.redeemAddress").value("18C379AC61A573459Dc6E6C2a5aDfFB86fe93a06"));
    }

    @Test
    public void shouldReturn200WhenSendingRequestForBlockToBlockController() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/block/0"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(1530261926))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockNum").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.previousBlockHash").value("0000000000000000000000000000000000000000000000000000000000000000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockHash").value("F96BD2EE2770B4CBD8BE49056DD1F88DC8F1CF462FAB142144214156EA0266AA"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.redeemAddress").value("18C379AC61A573459Dc6E6C2a5aDfFB86fe93a06"));
    }

    @Test
    @Ignore
    public void shouldReturn404WhenRequestBlockToBlockController() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/block/999"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
