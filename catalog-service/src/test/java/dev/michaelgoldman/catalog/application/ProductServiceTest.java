package dev.michaelgoldman.catalog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.domain.Product;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import dev.michaelgoldman.catalog.domain.ProductNotFoundException;
import org.junit.jupiter.api.Test;

class ProductServiceTest {

    private static final String SKU = "ADIDAS-SHOE-42";
    private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");

    private final InMemoryProductRepository repository = new InMemoryProductRepository();
    private final ProductService service = new ProductService(repository);

    @Test
    void upsert_whenNewSku_createsWithoutPrice() {
        UpsertResult result = service.upsert(command("Running shoe"));

        assertThat(result.created()).isTrue();
        Product saved = repository.findBySku(SKU).orElseThrow();
        assertThat(saved.getName()).isEqualTo("Running shoe");
        assertThat(saved.getPrice()).isEmpty();
    }

    @Test
    void upsert_whenExistingSku_replacesDetailsAndKeepsPrice() {
        Price price = new Price(new BigDecimal("89.95"), NOW);
        repository.save(new Product(SKU, "Old name", null, null, null, Map.of(), price));

        UpsertResult result = service.upsert(command("New name"));

        assertThat(result.created()).isFalse();
        Product saved = repository.findBySku(SKU).orElseThrow();
        assertThat(saved.getName()).isEqualTo("New name");
        assertThat(saved.getPrice()).contains(price);
    }

    @Test
    void get_whenUnknownSku_throws() {
        assertThatThrownBy(() -> service.get("UNKNOWN"))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void list_returnsAtMostSizeProductsSortedBySku() {
        saveProducts("C", "A", "B");

        ProductPage page = service.list(0, 2);

        assertThat(page.products()).extracting(Product::getSku).containsExactly("A", "B");
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void list_pagePastTheEnd_isEmpty() {
        saveProducts("C", "A", "B");

        ProductPage page = service.list(5, 2);

        assertThat(page.products()).isEmpty();
        assertThat(page.totalElements()).isEqualTo(3);
    }

    private static UpsertProductCommand command(String name) {
        return new UpsertProductCommand(SKU, name, null, null, null, Map.of());
    }

    // saves them as C, A, B, so the test checks that list sorts them to A, B, C
    private void saveProducts(String... skus) {
        for (String sku : skus) {
            repository.save(Product.create(sku, "Product " + sku, null, null, null, Map.of()));
        }
    }
}
