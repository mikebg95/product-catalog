package dev.michaelgoldman.catalog.application;

import dev.michaelgoldman.catalog.application.port.ProductRepository;
import dev.michaelgoldman.catalog.domain.Product;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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

    @Override
    public ProductPage findPage(int page, int size) {
        List<Product> pageOfProducts = products.values().stream()
                .sorted(Comparator.comparing(Product::getSku)) // sort by SKU
                .skip((long) page * size) // skip the earlier pages
                .limit(size) // take at most size
                .map(InMemoryProductRepository::copy) // makes copy of each product before handing it out
                .toList();
        return new ProductPage(pageOfProducts, page, size, products.size());
    }

    // stores and hands out copies
    private static Product copy(Product p) {
        return new Product(p.getSku(), p.getName(), p.getDescription(), p.getBrand(),
                p.getCategory(), p.getAttributes(), p.getPrice().orElse(null));
    }
}
