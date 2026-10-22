package dev.michaelgoldman.catalog.adapter.in.web;

import dev.michaelgoldman.catalog.api.model.PriceDto;
import dev.michaelgoldman.catalog.api.model.ProductDto;
import dev.michaelgoldman.catalog.api.model.ProductRequestDto;
import dev.michaelgoldman.catalog.application.UpsertProductCommand;
import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.domain.Product;

import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

@Component
class ProductWebMapper {

    UpsertProductCommand toCommand(String sku, ProductRequestDto request) {
        return new UpsertProductCommand(sku, request.getName(), request.getDescription(), request.getBrand(),
                request.getCategory(), request.getAttributes());
    }

    ProductDto toDto(Product product) {
        return new ProductDto(product.getSku(), product.getName(), product.getAttributes())
                .description(product.getDescription())
                .brand(product.getBrand())
                .category(product.getCategory())
                .price(product.getPrice().map(ProductWebMapper::toDto).orElse(null));
    }

    // the domain has no currency, the api always says EUR
    private static PriceDto toDto(Price price) {
        return new PriceDto(price.amount(), PriceDto.CurrencyEnum.EUR, price.updatedAt().atOffset(ZoneOffset.UTC));
    }
}
