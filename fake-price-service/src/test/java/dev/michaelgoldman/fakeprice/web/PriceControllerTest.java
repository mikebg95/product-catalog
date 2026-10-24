package dev.michaelgoldman.fakeprice.web;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.michaelgoldman.fakeprice.store.PriceStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PriceController.class)
@Import(PriceStore.class)
class PriceControllerTest {

    private static final String PRICE_BODY = """
            { "amount": 79.95 }
            """;

    @Autowired
    MockMvc mockMvc;

    @Test
    void setPrice_unregisteredSku_returns404() throws Exception {
        mockMvc.perform(put("/prices/{sku}", "NEVER-REGISTERED-1").contentType(MediaType.APPLICATION_JSON).content(PRICE_BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void registerSku_newThenAgain_returns201Then204() throws Exception {
        mockMvc.perform(put("/skus/{sku}", "REGISTER-TWICE-1"))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/skus/{sku}", "REGISTER-TWICE-1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listPrices_onlyReturnsSkusWithAPrice() throws Exception {
        mockMvc.perform(put("/skus/{sku}", "PRICED-1")).andExpect(status().isCreated());
        mockMvc.perform(put("/skus/{sku}", "NOT-PRICED-1")).andExpect(status().isCreated());
        mockMvc.perform(put("/prices/{sku}", "PRICED-1").contentType(MediaType.APPLICATION_JSON).content(PRICE_BODY))
                .andExpect(status().isOk());

        mockMvc.perform(get("/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prices[*].sku", hasItem("PRICED-1")))
                .andExpect(jsonPath("$.prices[*].sku", not(hasItem("NOT-PRICED-1"))));
    }
}
