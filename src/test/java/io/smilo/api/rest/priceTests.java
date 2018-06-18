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
import io.smilo.api.rest.models.Price;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;

public class priceTests extends SmiloApiTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void shouldReturn200WhenSendingRequestToPriceController() throws Exception {
        @SuppressWarnings("rawtypes")
        ResponseEntity<List> response = this.testRestTemplate.getForEntity(
                "http://localhost:" + port + "/price", List.class);

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldReturnValueWhenSendingRequestToPriceController() throws Exception {
        @SuppressWarnings("rawtypes")
        ResponseEntity<List> response = this.testRestTemplate.getForEntity(
                "http://localhost:" + port + "/price", List.class);

        List body = response.getBody();
        Assert.assertEquals("Test failed!",body.get(0).toString(), "{currencyFrom=XSM, currencyTo=USD, value=0.25}");
    }
}
