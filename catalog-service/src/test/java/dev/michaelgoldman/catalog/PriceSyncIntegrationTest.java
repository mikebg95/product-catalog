package dev.michaelgoldman.catalog;

import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.michaelgoldman.catalog.application.PriceSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

// the whole price sync with a real postgres: wiremock plays the price system, the price ends up in GET /products/{sku}
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class PriceSyncIntegrationTest {

    private static final String SKU = "E2E-BAG-1";

    @RegisterExtension
    static WireMockExtension priceSystem = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void priceSystemProperties(DynamicPropertyRegistry registry) {
        registry.add("price-system.base-url", priceSystem::baseUrl);
        // the test runs the round itself, so the scheduler stays out of the way
        registry.add("price-sync.interval", () -> "1h");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PriceSyncService priceSyncService;

    @Test
    void syncedPriceShowsUpInTheApi() throws Exception {
        mockMvc.perform(put("/products/{sku}", SKU).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Blue bag" }
                                """))
                .andExpect(status().isCreated());
        priceSystem.stubFor(WireMock.get("/prices").willReturn(okJson("""
                { "prices": [
                    { "sku": "E2E-BAG-1",
                      "price": { "amount": 79.95, "currency": "EUR", "updatedAt": "2026-10-18T12:00:00Z" } }
                ] }
                """)));

        priceSyncService.syncPrices();

        mockMvc.perform(get("/products/{sku}", SKU))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price.amount").value(79.95))
                .andExpect(jsonPath("$.price.updatedAt").value("2026-10-18T12:00:00Z"));
    }
}
