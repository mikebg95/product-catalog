package dev.michaelgoldman.catalog.adapter.out.price;

import dev.michaelgoldman.catalog.priceclient.ApiClient;
import dev.michaelgoldman.catalog.priceclient.api.PricesApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PriceClientConfig {

    @Bean
    PricesApi pricesApi() {
        ApiClient apiClient = new ApiClient().setBasePath("http://localhost:8081");
        return new PricesApi(apiClient);
    }
}
