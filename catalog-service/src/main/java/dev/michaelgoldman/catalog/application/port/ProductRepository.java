package dev.michaelgoldman.catalog.application.port;

import dev.michaelgoldman.catalog.application.ProductPage;
import dev.michaelgoldman.catalog.domain.Product;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository {
    Optional<Product> findBySku(String sku);
    void save(Product product);
    ProductPage findPage(int page, int size);
    ProductPage search(String q, int page, int size);
}
