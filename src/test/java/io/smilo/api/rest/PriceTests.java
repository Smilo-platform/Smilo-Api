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

public class PriceTests extends AbstractWebSpringTest {

    @Test
    public void shouldReturn200WhenSendingRequestToPriceController() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/price"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].currencyFrom").value("XSM"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].currencyTo").value("USD"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].value").value(0.25))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].currencyFrom").value("XSM"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].currencyTo").value("ETH"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[1].value").value(0.05))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].currencyFrom").value("XSM"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].currencyTo").value("BTC"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[2].value").value(0.005))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[3].currencyFrom").value("XSP"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[3].currencyTo").value("XSM"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[3].value").value(0.2));
    }
}
