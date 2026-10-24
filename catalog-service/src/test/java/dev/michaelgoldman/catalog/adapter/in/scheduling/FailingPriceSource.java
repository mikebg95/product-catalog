package dev.michaelgoldman.catalog.adapter.in.scheduling;

import dev.michaelgoldman.catalog.application.port.PriceSource;
import dev.michaelgoldman.catalog.application.port.PriceSourceUnavailableException;
import dev.michaelgoldman.catalog.domain.Price;

import java.util.Map;

public class FailingPriceSource implements PriceSource {
    @Override
    public Map<String, Price> fetchPrices() {
        throw new PriceSourceUnavailableException("price system down", null);
    }
}
