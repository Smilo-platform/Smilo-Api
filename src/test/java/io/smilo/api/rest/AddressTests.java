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
import io.smilo.api.TestUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class AddressTests extends AbstractWebSpringTest {

    @Autowired
    private TestUtility testUtility;

    @Test
    public void shouldReturn200WhenSendingRequestToAddressController() throws Exception {
        testUtility.addBlockZero();
        this.webClient.perform(MockMvcRequestBuilders.get("/address/S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.address").value("S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.balances.000x00123").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.balances.000x00123").value(200000000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.signatureCount").value(1));
    }

    @Test
    public void shouldReturn404WhenSendingRequestToAddressControllerIfNotExist() throws Exception {
        testUtility.addBlockZero();
        this.webClient.perform(MockMvcRequestBuilders.get("/address/ETm9QUJLVdJkTqRojTNqswmeAQGaofojJJ"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }
}