package dev.michaelgoldman.catalog.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {

    // also an exact sku match: full-text search misses skus like GEN-0000042 ("-0000042" is read as a number)
    @Query(value = """
              SELECT * FROM products
              WHERE search_vector @@ to_tsquery('english', :query) OR sku = :sku
              ORDER BY sku = :sku DESC, ts_rank(search_vector, to_tsquery('english', :query)) DESC, sku
              """,
            countQuery = """
              SELECT count(*) FROM products
              WHERE search_vector @@ to_tsquery('english', :query) OR sku = :sku
              """,
            nativeQuery = true)
    Page<ProductEntity> search(@Param("query") String query, @Param("sku") String sku, Pageable pageable);
}
