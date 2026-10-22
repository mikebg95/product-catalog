package dev.michaelgoldman.catalog.adapter.out.persistence;

import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.domain.Product;

import org.springframework.stereotype.Component;

@Component
class ProductEntityMapper {

    Product toDomain(ProductEntity entity) {
        Price price = entity.priceAmount == null ? null : new Price(entity.priceAmount, entity.priceUpdatedAt);
        return new Product(entity.sku, entity.name, entity.description, entity.brand, entity.category,
                entity.attributes, price);
    }

    ProductEntity toEntity(Product product) {
        ProductEntity entity = new ProductEntity(product.getSku());
        entity.name = product.getName();
        entity.description = product.getDescription();
        entity.brand = product.getBrand();
        entity.category = product.getCategory();
        entity.attributes = product.getAttributes();
        entity.priceAmount = product.getPrice().map(Price::amount).orElse(null);
        entity.priceUpdatedAt = product.getPrice().map(Price::updatedAt).orElse(null);
        return entity;
    }
}
