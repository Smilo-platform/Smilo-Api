package io.smilo.api.rest;

import io.smilo.api.AbstractWebSpringTest;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class TxTests extends AbstractWebSpringTest {
    @Test
    public void shouldReturn404WhenRequestingUnknownTransaction() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/tx/unknown_tx_hash"))
                .andExpect(MockMvcResultMatchers.status().is(404));
    }

    @Test
    public void shouldReturn200WhenRequestingKnownTransaction() throws Exception {
        throw new NotImplementedException("Not implemented");
    }

    @Test
    public void shouldReturnCorrectTransaction() throws Exception {
        throw new NotImplementedException("Not implemented");
    }

    @Test
    public void shouldReturnCorrectListOfTransactions() throws Exception {
        throw new NotImplementedException("Not implemented");
    }
}
