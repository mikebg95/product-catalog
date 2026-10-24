package dev.michaelgoldman.fakeprice.store;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PriceStore {
    private final Set<String> registeredSkus = ConcurrentHashMap.newKeySet();
    private final Map<String, StoredPrice> prices = new ConcurrentHashMap<>();

    public StoredPrice findBySku(String sku) {
        return prices.get(sku);
    }

    public boolean registerSku(String sku) {
        return registeredSkus.add(sku);
    }

    public Set<String> listSkus() {
        return registeredSkus;
    }

    public boolean setPrice(String sku, StoredPrice price) {
        if (!registeredSkus.contains(sku)) {
            return false; // unknown SKU
        }

        prices.put(sku, price);
        return true;
    }

    public Map<String, StoredPrice> getStoredPrices() {
        return prices;
    }
}
