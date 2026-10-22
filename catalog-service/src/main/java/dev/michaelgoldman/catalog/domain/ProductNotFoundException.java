package dev.michaelgoldman.catalog.domain;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String sku) {
        super("No product found with SKU " + sku);
    }
}
