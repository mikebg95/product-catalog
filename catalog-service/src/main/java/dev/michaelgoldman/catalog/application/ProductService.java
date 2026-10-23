package dev.michaelgoldman.catalog.application;

import dev.michaelgoldman.catalog.application.port.ProductRepository;
import dev.michaelgoldman.catalog.domain.Product;

import java.util.Optional;

import dev.michaelgoldman.catalog.domain.ProductNotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    // create or replace by SKU: replace keeps the existing price; only price sync can change price
    @Transactional
    public UpsertResult upsert(UpsertProductCommand command) {
        Optional<Product> existing = repository.findBySku(command.sku());

        Product product;
        if (existing.isPresent()) {
            product = existing.get();
            product.replaceDetails(command.name(), command.description(), command.brand(),
                    command.category(), command.attributes());
        } else {
            product = Product.create(command.sku(), command.name(), command.description(),
                    command.brand(), command.category(), command.attributes());
        }

        repository.save(product);
        return new UpsertResult(product, existing.isEmpty());
    }

    public Product get(String sku) {
        Optional<Product> optional = repository.findBySku(sku);

        if (optional.isEmpty()) {
            throw new ProductNotFoundException(sku);
        }
        return optional.get();
    }

    public ProductPage list(int page, int size) {
        return repository.findPage(page, size);
    }

    public ProductPage search(@Nullable String q, int page, int size) {
        if (q == null || q.isBlank()) {
            return list(page, size);
        }
        return repository.search(q, page, size);
    }
}
