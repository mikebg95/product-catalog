package dev.michaelgoldman.catalog.adapter.out.price;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("price-system")
public record PriceSystemProperties(
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        int maxRetries,
        Duration retryDelay) {
}
