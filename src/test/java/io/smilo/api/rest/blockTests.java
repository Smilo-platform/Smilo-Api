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

public class blockTests extends AbstractWebSpringTest {

    @Test
    @Ignore //Todo: add blocks
    public void shouldReturn200WhenSendingRequestToBlockController() throws Exception {

        this.webClient.perform(MockMvcRequestBuilders.get("/block"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @Ignore //Todo: add block
    public void shouldReturn200WhenSendingRequestForBlockToBlockController() throws Exception {

        this.webClient.perform(MockMvcRequestBuilders.get("/block/1"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void shouldReturn404WhenRequestBlockToBlockController() throws Exception {

        this.webClient.perform(MockMvcRequestBuilders.get("/block/100"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}
