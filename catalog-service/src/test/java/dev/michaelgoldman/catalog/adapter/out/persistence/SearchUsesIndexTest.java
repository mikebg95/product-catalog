package dev.michaelgoldman.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import dev.michaelgoldman.catalog.TestcontainersConfiguration;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

// the "search under 1 second" requirement, tested by its cause: the search must use the GIN index, not read every product
// a timing test would be slow and flaky; the speed itself is measured with a million products (scripts/seed-million.sh)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class SearchUsesIndexTest {

    private static final String INDEX = "products_search_vector_idx";
    // "trail runner", the way the adapter turns it into a search instruction
    private static final String SEARCH = "'trail:* & runner:*'";
    private static final String SKU_SEARCH = "'TRAIL RUNNER'";

    @Autowired
    EntityManager entityManager;

    // enough products that a full scan would be the slow choice, so postgres picks the index for a real reason
    @BeforeEach
    void manyProducts() {
        entityManager.createNativeQuery("""
                INSERT INTO products (sku, name, description, brand, category)
                SELECT 'GEN-' || i, 'Product ' || i, 'A sturdy item number ' || i, 'Brand ' || (i % 50), 'Category ' || (i % 10)
                FROM generate_series(1, 20000) AS i
                """).executeUpdate();
        entityManager.createNativeQuery("""
                INSERT INTO products (sku, name, brand, category) VALUES ('SHOE-RUN-42', 'Trail Runner 2', 'Nordwind', 'Shoes')
                """).executeUpdate();
        entityManager.createNativeQuery("ANALYZE products").executeUpdate();
    }

    @Test
    void search_usesTheSearchIndex() throws Exception {
        // the real query from ProductJpaRepository, so this test can't drift from the code; spring adds the limit for a page
        String plan = explain(searchQuery().value() + " LIMIT 20");

        assertThat(plan).contains(INDEX).doesNotContain("Seq Scan on products");
    }

    @Test
    void searchCount_usesTheSearchIndex() throws Exception {
        String plan = explain(searchQuery().countQuery());

        assertThat(plan).contains(INDEX).doesNotContain("Seq Scan on products");
    }

    private static Query searchQuery() throws NoSuchMethodException {
        return ProductJpaRepository.class.getMethod("search", String.class, String.class, Pageable.class).getAnnotation(Query.class);
    }

    private String explain(String sql) {
        @SuppressWarnings("unchecked")
        List<String> lines = entityManager.createNativeQuery("EXPLAIN " + sql.replace(":query", SEARCH).replace(":sku", SKU_SEARCH)).getResultList();
        return lines.stream().collect(Collectors.joining("\n"));
    }
}
