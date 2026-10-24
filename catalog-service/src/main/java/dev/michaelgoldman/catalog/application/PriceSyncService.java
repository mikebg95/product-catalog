package dev.michaelgoldman.catalog.application;

import dev.michaelgoldman.catalog.application.port.PriceSource;
import dev.michaelgoldman.catalog.application.port.ProductRepository;
import dev.michaelgoldman.catalog.domain.Price;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PriceSyncService {
    private final ProductRepository repository;
    private final PriceSource priceSource;

    public PriceSyncService(ProductRepository repository, PriceSource priceSource) {
        this.repository = repository;
        this.priceSource = priceSource;
    }

    // unknown skus are skipped; a product is only saved when its price was newer
    public void syncPrices() {
        for (Map.Entry<String, Price> entry : priceSource.fetchPrices().entrySet()) {
            repository.findBySku(entry.getKey()).ifPresent(product -> {
                if (product.applyPrice(entry.getValue())) {
                    repository.save(product);
                }
            });
        }
    }
}
