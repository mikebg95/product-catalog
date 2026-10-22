package dev.michaelgoldman.catalog.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface ProductJpaRepository extends JpaRepository<ProductEntity, String> {
}
