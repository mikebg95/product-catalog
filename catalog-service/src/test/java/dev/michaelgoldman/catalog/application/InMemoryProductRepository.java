package dev.michaelgoldman.catalog.application;

import dev.michaelgoldman.catalog.application.port.ProductRepository;
import dev.michaelgoldman.catalog.domain.Product;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryProductRepository implements ProductRepository {
    private final Map<String, Product> products = new HashMap<>();

    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(products.get(sku)).map(InMemoryProductRepository::copy);
    }

    @Override
    public void save(Product product) {
        products.put(product.getSku(), copy(product));
    }

    // stores and hands out copies
    private static Product copy(Product p) {
        return new Product(p.getSku(), p.getName(), p.getDescription(), p.getBrand(),
                p.getCategory(), p.getAttributes(), p.getPrice().orElse(null));
    }
}
