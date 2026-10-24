package dev.michaelgoldman.catalog.adapter.out.price;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import dev.michaelgoldman.catalog.application.port.PriceSourceUnavailableException;
import dev.michaelgoldman.catalog.priceclient.ApiClient;
import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.priceclient.api.PricesApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class PriceClientAdapterTest {

    private PriceClientAdapter adapter;

    @RegisterExtension
    static WireMockExtension priceSystem = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    void setup() {
        ApiClient apiClient = new ApiClient().setBasePath(priceSystem.baseUrl());
        adapter = new PriceClientAdapter(new PricesApi(apiClient));
    }

    @Test
    void fetchPrices_validResponse_returnsPricePerSku() {
        priceSystem.stubFor(get("/prices").willReturn(okJson("""
                { "prices": [
                    { "sku": "SHOE-RED-42",
                      "price": { "amount": 89.95, "currency": "EUR", "updatedAt": "2026-10-18T12:00:00Z" } },
                    { "sku": "BAG-BLUE-1",
                      "price": { "amount": 60, "currency": "EUR", "updatedAt": "2026-10-18T14:00:00+02:00" } }
                ] }
                """)));

        Map<String, Price> prices = adapter.fetchPrices();

        assertThat(prices).containsOnly(
                entry("SHOE-RED-42", new Price(new BigDecimal("89.95"), Instant.parse("2026-10-18T12:00:00Z"))),
                // +02:00 is the same moment as 12:00 UTC
                entry("BAG-BLUE-1", new Price(new BigDecimal("60.00"), Instant.parse("2026-10-18T12:00:00Z"))));
    }

    @Test
    void fetchPrices_serverError_throws() {
        priceSystem.stubFor(get("/prices").willReturn(serverError()));

        assertThatThrownBy(() -> adapter.fetchPrices())
                .isInstanceOf(PriceSourceUnavailableException.class);
    }

    @Test
    void fetchPrices_priceSystemUnreachable_throws() {
        priceSystem.stubFor(get("/prices").willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));

        assertThatThrownBy(() -> adapter.fetchPrices())
                .isInstanceOf(PriceSourceUnavailableException.class);
    }

    @Disabled("retries come later")
    @Test
    void fetchPrices_firstCallFails_retriesAndReturnsPrices() {

    }
}
