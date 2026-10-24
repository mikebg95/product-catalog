package dev.michaelgoldman.catalog.adapter.out.price;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import dev.michaelgoldman.catalog.application.port.PriceSourceUnavailableException;
import dev.michaelgoldman.catalog.domain.Price;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.serviceUnavailable;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class PriceClientAdapterTest {

    // short, so the "too slow" test doesn't wait long
    private static final Duration CONNECT_TIMEOUT = Duration.ofMillis(500);
    private static final Duration READ_TIMEOUT = Duration.ofMillis(500);
    // 2 retries = 3 attempts; a tiny pause, so the failing tests stay fast
    private static final int MAX_RETRIES = 2;
    private static final Duration RETRY_DELAY = Duration.ofMillis(10);

    private PriceClientAdapter adapter;

    @RegisterExtension
    static WireMockExtension priceSystem = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @BeforeEach
    void setup() {
        // the same client and retry construction as the app, only with WireMock's url and short timings
        adapter = new PriceClientAdapter(
                PriceClientConfig.createPricesApi(priceSystem.baseUrl(), CONNECT_TIMEOUT, READ_TIMEOUT),
                PriceClientConfig.createRetryTemplate(MAX_RETRIES, RETRY_DELAY));
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

    @Test
    void fetchPrices_priceSystemTooSlow_throws() {
        // answers, but only after the read timeout has passed
        priceSystem.stubFor(get("/prices").willReturn(okJson("{ \"prices\": [] }").withFixedDelay(2000)));

        assertThatThrownBy(() -> adapter.fetchPrices())
                .isInstanceOf(PriceSourceUnavailableException.class);
    }

    @Test
    void fetchPrices_firstCallFails_retriesAndReturnsPrices() {
        // a scenario: wiremock remembers a state, so the first and second call get a different answer
        priceSystem.stubFor(get("/prices").inScenario("blip")
                .whenScenarioStateIs(Scenario.STARTED)
                .willReturn(serviceUnavailable())
                .willSetStateTo("back up"));
        priceSystem.stubFor(get("/prices").inScenario("blip")
                .whenScenarioStateIs("back up")
                .willReturn(okJson("""
                        { "prices": [
                            { "sku": "SHOE-RED-42",
                              "price": { "amount": 89.95, "currency": "EUR", "updatedAt": "2026-10-18T12:00:00Z" } }
                        ] }
                        """)));

        Map<String, Price> prices = adapter.fetchPrices();

        assertThat(prices).containsOnly(
                entry("SHOE-RED-42", new Price(new BigDecimal("89.95"), Instant.parse("2026-10-18T12:00:00Z"))));
        priceSystem.verify(2, getRequestedFor(urlEqualTo("/prices")));
    }
}
