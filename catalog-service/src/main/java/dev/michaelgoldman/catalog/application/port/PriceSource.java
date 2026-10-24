package dev.michaelgoldman.catalog.application.port;

import dev.michaelgoldman.catalog.domain.Price;

import java.util.Map;

public interface PriceSource {
    // sku -> current price
    Map<String, Price> fetchPrices();
}
