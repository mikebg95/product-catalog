package dev.michaelgoldman.catalog.adapter.in.web;

import dev.michaelgoldman.catalog.api.ProductsApi;
import dev.michaelgoldman.catalog.api.model.ProductDto;
import dev.michaelgoldman.catalog.api.model.ProductPageDto;
import dev.michaelgoldman.catalog.api.model.ProductRequestDto;
import dev.michaelgoldman.catalog.application.ProductService;
import dev.michaelgoldman.catalog.application.UpsertResult;
import dev.michaelgoldman.catalog.domain.ProductNotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController implements ProductsApi {
    private final ProductService productService;
    private final ProductWebMapper productWebMapper;

    public ProductController(ProductService productService, ProductWebMapper productWebMapper) {
        this.productService = productService;
        this.productWebMapper = productWebMapper;
    }

    @Override
    public ResponseEntity<ProductDto> getProduct(String sku) throws ProductNotFoundException {
            return ResponseEntity.ok().body(productWebMapper.toDto(productService.get(sku)));
    }

    @Override
    public ResponseEntity<ProductPageDto> listProducts(Integer page, Integer size, @Nullable String q) {
        return ResponseEntity.ok(productWebMapper.toPageDto(productService.search(q, page, size)));
    }

    @Override
    public ResponseEntity<ProductDto> upsertProduct(String sku, ProductRequestDto productRequestDto) {
        UpsertResult upsertResult = productService.upsert(productWebMapper.toCommand(sku, productRequestDto));
        HttpStatus status = upsertResult.created() ? HttpStatus.CREATED : HttpStatus.OK;

        return ResponseEntity.status(status).body(productWebMapper.toDto(upsertResult.product()));
    }
}
