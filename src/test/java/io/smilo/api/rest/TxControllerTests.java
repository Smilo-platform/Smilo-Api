package io.smilo.api.rest;

import io.smilo.api.AbstractWebSpringTest;
import io.smilo.api.TestUtility;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class TxControllerTests extends AbstractWebSpringTest {

    @Before
    public void initialize() {
//        testUtility.addBlockZero();
    }

    @Test
    public void shouldReturn404WhenRequestingUnknownTransaction() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/tx/unknown_tx_hash"))
                .andExpect(MockMvcResultMatchers.status().is(404));
    }

    @Test
    public void shouldReturn200WhenRequestingKnownTransaction() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/tx/7E6F401FFFD55A1AF05E666D7CF1CD5B38628D3E6FC0CFB9935E82D9BA5329B3"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void shouldReturnCorrectTransaction() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/tx/7E6F401FFFD55A1AF05E666D7CF1CD5B38628D3E6FC0CFB9935E82D9BA5329B3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.assetId").value("000x00123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.inputAmount").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactionOutputs[0].outputAddress").value("S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactionOutputs[0].outputAmount").value("200000000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").value(1514764800000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.inputAddress").value("S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.fee").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.signatureIndex").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.dataHash").value("7E6F401FFFD55A1AF05E666D7CF1CD5B38628D3E6FC0CFB9935E82D9BA5329B3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.blockIndex").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.version").value(1));
    }

    @Test
    public void shouldReturn200WhenRequestingTransactionList() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/tx"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void shouldReturnCorrectListOfTransactions() throws Exception {
        this.webClient.perform(MockMvcRequestBuilders.get("/tx"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.skip").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.take").value(32))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].assetId").value("000x00123"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].inputAmount").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].transactionOutputs[0].outputAddress").value("S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].transactionOutputs[0].outputAmount").value("200000000"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].timestamp").value(1514764800000L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].inputAddress").value("S1RQ3ZVRQ2K42FTXDONQVFVX73Q37JHIDCSFAR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].fee").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].signatureIndex").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].dataHash").value("7E6F401FFFD55A1AF05E666D7CF1CD5B38628D3E6FC0CFB9935E82D9BA5329B3"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].blockIndex").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.transactions[0].version").value(1));
    }
}
