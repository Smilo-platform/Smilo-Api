package io.smilo.api.rest;

import io.smilo.api.AbstractWebSpringTest;
import io.smilo.api.TestUtility;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class AssetControllerTests extends AbstractWebSpringTest {
    @Autowired
    private TestUtility testUtility;

    @Test
    public void shouldReturn404WhenRequestingUnknownAsset() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/asset/SomeNonExistingAsset"))
                .andExpect(MockMvcResultMatchers.status().is(404));
    }

    @Test
    public void shouldReturn200WhenRequestingKnownAsset() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/asset/000x00123"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void shouldReturnCorrectAsset() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/asset/000x00123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.address").value("000x00123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalSupply").value(200000000))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Smilo"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.decimals").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.symbol").value("XSM"));
    }
}
