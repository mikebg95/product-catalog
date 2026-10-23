package dev.michaelgoldman.catalog.adapter.in.web;

import dev.michaelgoldman.catalog.api.model.PriceDto;
import dev.michaelgoldman.catalog.api.model.ProductDto;
import dev.michaelgoldman.catalog.api.model.ProductPageDto;
import dev.michaelgoldman.catalog.api.model.ProductRequestDto;
import dev.michaelgoldman.catalog.application.ProductPage;
import dev.michaelgoldman.catalog.application.UpsertProductCommand;
import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.domain.Product;

import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class ProductWebMapper {

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

    ProductPageDto toPageDto(ProductPage page) {
        List<ProductDto> content = page.products().stream().map(this::toDto).toList();
        // rounded up: 3 products with 2 per page is 2 pages
        int totalPages = (int) ((page.totalElements() + page.size() - 1) / page.size());
        return new ProductPageDto(content, page.page(), page.size(), page.totalElements(), totalPages);
    }

    // the domain has no currency, the api always says EUR
    private static PriceDto toDto(Price price) {
        return new PriceDto(price.amount(), PriceDto.CurrencyEnum.EUR, price.updatedAt().atOffset(ZoneOffset.UTC));
    }
}
