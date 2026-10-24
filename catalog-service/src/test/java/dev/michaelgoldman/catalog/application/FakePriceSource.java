package dev.michaelgoldman.catalog.application;

import dev.michaelgoldman.catalog.application.port.PriceSource;
import dev.michaelgoldman.catalog.domain.Price;

import java.util.HashMap;
import java.util.Map;

public class FakePriceSource implements PriceSource {
    private final Map<String, Price> prices = new HashMap<>();

    void add(String sku, Price price) {
        prices.put(sku, price);
    }

    @Override
    public Map<String, Price> fetchPrices() {
        return prices;
    }
}
