package dev.michaelgoldman.catalog.adapter.out.persistence;

import dev.michaelgoldman.catalog.application.port.ProductRepository;
import dev.michaelgoldman.catalog.domain.Product;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class ProductPersistenceAdapter implements ProductRepository {
    private final ProductJpaRepository productJpaRepository;
    private final ProductEntityMapper mapper;

    public ProductPersistenceAdapter(ProductJpaRepository productJpaRepository, ProductEntityMapper mapper) {
        this.productJpaRepository = productJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return productJpaRepository.findById(sku).map(mapper::toDomain);
    }

    @Override
    public void save(Product product) {
        ProductEntity entity = mapper.toEntity(product);
        if (productJpaRepository.existsById(product.getSku())) {
            entity.markNotNew();
        }

        productJpaRepository.save(entity);
    }
}
