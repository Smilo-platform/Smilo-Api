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
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class NetworkControllerTests extends AbstractWebSpringTest {

    @Test
    public void shouldReturn200WhenSendingRequestToNetworkController() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/network"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nodes").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.edges").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network").exists());
    }

    @Test
    public void shouldReturn200WhenSendingNetworkRequestToNetworkController() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/network/XeqfS1JV21Z7KsiG"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.nodes").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.edges").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.XeqfS1JV21Z7KsiG").exists());
    }

}
