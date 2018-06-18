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

import io.smilo.api.SmiloApiTests;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.internal.bytebuddy.matcher.ElementMatchers.is;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;

public class balanceTests extends SmiloApiTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void shouldReturn200WhenSendingRequestToBalanceController() throws Exception {
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = this.testRestTemplate.getForEntity(
                "http://localhost:" + port + "/balance/test", Map.class);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturn200WhenSendingRequestToBalanceControllerWithBalance() throws Exception {
        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> response = this.testRestTemplate.getForEntity(
                "http://localhost:" + port + "/balance/ETm9QUJLVdJkTqRojTNqswmeAQGaofojJJ", Map.class);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Assert.assertEquals("Test failed!",response.getBody().get("publicKey").toString(), "ETm9QUJLVdJkTqRojTNqswmeAQGaofojJJ");
        Assert.assertEquals("Test failed!",response.getBody().get("storedCoins").toString(), "[{currency=XSM, amount=5712.0}, {currency=XSP, amount=234.0}]");
    }
}
