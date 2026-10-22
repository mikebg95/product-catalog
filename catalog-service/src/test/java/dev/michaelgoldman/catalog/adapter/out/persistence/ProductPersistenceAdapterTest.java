package dev.michaelgoldman.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.michaelgoldman.catalog.TestcontainersConfiguration;
import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.domain.Product;

import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

// runs against a real postgres in docker, with the flyway migration
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ TestcontainersConfiguration.class, ProductPersistenceAdapter.class, ProductEntityMapper.class })
class ProductPersistenceAdapterTest {

    private static final String SKU = "ADIDAS-SHOE-42";
    private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");

    @Autowired
    ProductPersistenceAdapter adapter;

    @Autowired
    EntityManager entityManager;

    @Test
    void savesAndLoadsNewProductWithoutPrice() {
        adapter.save(Product.create(SKU, "Running shoe", "Light", "Adidas", "Shoes", Map.of("size", 42)));
        clearCache();

        Product loaded = adapter.findBySku(SKU).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Running shoe");
        assertThat(loaded.getAttributes()).containsEntry("size", 42);
        assertThat(loaded.getPrice()).isEmpty();
    }

    @Test
    void savingExistingProductUpdatesItAndKeepsPrice() {
        Price price = new Price(new BigDecimal("89.95"), NOW);
        adapter.save(new Product(SKU, "Old name", null, null, null, Map.of(), price));
        clearCache();

        adapter.save(new Product(SKU, "New name", null, null, null, Map.of(), price));
        clearCache();

        Product loaded = adapter.findBySku(SKU).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("New name");
        assertThat(loaded.getPrice()).contains(price);
    }

    @Test
    void unknownSkuIsEmpty() {
        assertThat(adapter.findBySku("UNKNOWN")).isEmpty();
    }

    // write to the database and empty hibernate's cache, so the next read really comes from the database
    private void clearCache() {
        entityManager.flush();
        entityManager.clear();
    }
}
