package dev.michaelgoldman.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.michaelgoldman.catalog.TestcontainersConfiguration;
import dev.michaelgoldman.catalog.application.ProductPage;
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

    @Test
    void findPageReturnsOnePageSortedBySku() {
        // saves them as C, A, B, so the test checks that the database sorts them
        for (String sku : new String[] { "C", "A", "B" }) {
            adapter.save(Product.create(sku, "Product " + sku, null, null, null, Map.of()));
        }
        clearCache();

        ProductPage page = adapter.findPage(0, 2);

        assertThat(page.products()).extracting(Product::getSku).containsExactly("A", "B");
        assertThat(page.totalElements()).isEqualTo(3);
    }

    @Test
    void searchFindsProductsWithAllWordsAlsoWhenPartlyTyped() {
        adapter.save(Product.create("SHOE-RUN-42", "Red running shoes", null, "Nike", "Shoes", Map.of()));
        adapter.save(Product.create("CAP-RED", "Red cap", null, "Nike", "Caps", Map.of()));
        adapter.save(Product.create("SHOE-HIKE-40", "Hiking shoes", null, "Salomon", "Shoes", Map.of()));
        clearCache();

        // "sho" is not finished yet, but still finds "shoes"
        ProductPage page = adapter.search("red sho", 0, 20);

        assertThat(page.products()).extracting(Product::getSku).containsExactly("SHOE-RUN-42");
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void searchShowsBestMatchFirst() {
        // the backpack only mentions laptop in its description, the laptop has it in its name
        adapter.save(Product.create("BAG-01", "Backpack", "Has a laptop sleeve", null, null, Map.of()));
        adapter.save(Product.create("LAPTOP-14", "Laptop 14 inch", null, null, null, Map.of()));
        clearCache();

        ProductPage page = adapter.search("laptop", 0, 20);

        assertThat(page.products()).extracting(Product::getSku).containsExactly("LAPTOP-14", "BAG-01");
    }

    @Test
    void searchWithOnlySpecialCharactersFindsNothingAndDoesNotFail() {
        adapter.save(Product.create("CAP-RED", "Red cap", null, null, null, Map.of()));
        clearCache();

        ProductPage page = adapter.search("!!!", 0, 20);

        assertThat(page.products()).isEmpty();
        assertThat(page.totalElements()).isZero();
    }

    // write to the database and empty hibernate's cache, so the next read really comes from the database
    private void clearCache() {
        entityManager.flush();
        entityManager.clear();
    }
}
