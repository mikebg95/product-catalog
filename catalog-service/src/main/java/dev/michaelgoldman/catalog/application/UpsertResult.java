package dev.michaelgoldman.catalog.application;

import dev.michaelgoldman.catalog.domain.Product;

// what upsert gives back: the saved product, and whether it was new (201) or replaced (200)
public record UpsertResult(Product product, boolean created) {
}
