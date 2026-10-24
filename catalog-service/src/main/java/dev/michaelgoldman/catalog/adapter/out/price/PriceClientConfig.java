package dev.michaelgoldman.catalog.adapter.out.price;

import dev.michaelgoldman.catalog.priceclient.ApiClient;
import dev.michaelgoldman.catalog.priceclient.api.PricesApi;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(PriceSystemProperties.class)
public class PriceClientConfig {

    @Bean
    PricesApi pricesApi(PriceSystemProperties properties) {
        return createPricesApi(properties.baseUrl(), properties.connectTimeout(), properties.readTimeout());
    }

    @Bean
    RetryTemplate priceRetryTemplate(PriceSystemProperties properties) {
        return createRetryTemplate(properties.maxRetries(), properties.retryDelay());
    }

    // used by the bean above and by the test, so both build the client the same way
    static PricesApi createPricesApi(String baseUrl, Duration connectTimeout, Duration readTimeout) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        // keeps the generator's json settings, adds the timeouts
        RestClient restClient = ApiClient.buildRestClientBuilder()
                .requestFactory(requestFactory)
                .build();

        ApiClient apiClient = new ApiClient(restClient).setBasePath(baseUrl);
        return new PricesApi(apiClient);
    }

    // used by the bean above and by the test, so both retry the same way
    static RetryTemplate createRetryTemplate(int maxRetries, Duration delay) {
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(maxRetries)
                .delay(delay)
                // only what can be temporary: not reachable, too slow, or a 5xx answer; never a 4xx
                .predicate(e -> e instanceof ResourceAccessException || e instanceof HttpServerErrorException)
                .build();

        return new RetryTemplate(retryPolicy);
    }
}
