package dev.michaelgoldman.catalog.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {

    @Query(value = """
              SELECT * FROM products
              WHERE search_vector @@ to_tsquery('english', :query)
              ORDER BY ts_rank(search_vector, to_tsquery('english', :query)) DESC, sku
              """,
            countQuery = """
              SELECT count(*) FROM products
              WHERE search_vector @@ to_tsquery('english', :query)
              """,
            nativeQuery = true)
    Page<ProductEntity> search(@Param("query") String query, Pageable pageable);
}
