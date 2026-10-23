package dev.michaelgoldman.catalog.application;

import dev.michaelgoldman.catalog.domain.Product;

import java.util.List;

// one page of products, plus how many products there are in total
public record ProductPage(List<Product> products, int page, int size, long totalElements) {
}