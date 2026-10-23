package dev.michaelgoldman.catalog.adapter.out.persistence;

import dev.michaelgoldman.catalog.application.ProductPage;
import dev.michaelgoldman.catalog.application.port.ProductRepository;
import dev.michaelgoldman.catalog.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
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

    @Override
    public ProductPage findPage(int page, int size) {
        Page<ProductEntity> productPage = productJpaRepository.findAll(PageRequest.of(page, size, Sort.by("sku")));

        List<Product> products = productPage.getContent().stream().map(mapper::toDomain).toList();
        return new ProductPage(products, page, size, productPage.getTotalElements());
    }
}
