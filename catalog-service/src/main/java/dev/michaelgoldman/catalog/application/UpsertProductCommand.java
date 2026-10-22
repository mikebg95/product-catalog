package dev.michaelgoldman.catalog.application;

import java.util.Map;

// The input of the upsert use case. No price: a PUT never changes it.
public record UpsertProductCommand(
        String sku,
        String name,
        String description,
        String brand,
        String category,
        Map<String, Object> attributes) {
}
