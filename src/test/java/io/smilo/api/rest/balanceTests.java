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

public class balanceTests extends AbstractWebSpringTest {

    @Test
    public void shouldReturn200WhenSendingRequestToBalanceController() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/balance/test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.publicKey").value("test"));
    }

    @Test
    public void shouldReturn200WhenSendingRequestToBalanceControllerWithBalance() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/balance/ETm9QUJLVdJkTqRojTNqswmeAQGaofojJJ"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.publicKey").value("ETm9QUJLVdJkTqRojTNqswmeAQGaofojJJ"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.storedCoins[0].currency").value("XSM"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.storedCoins[0].amount").value(5712.0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.storedCoins[1].currency").value("XSP"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.storedCoins[1].amount").value(234.0));
    }
}
