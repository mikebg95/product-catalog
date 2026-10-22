package dev.michaelgoldman.catalog.application;

import java.util.Map;

// input for upsert, no price: a put never changes the price
public record UpsertProductCommand(
        String sku,
        String name,
        String description,
        String brand,
        String category,
        Map<String, Object> attributes) {
}
