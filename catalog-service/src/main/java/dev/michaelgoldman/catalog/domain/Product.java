package dev.michaelgoldman.catalog.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class Product {

	private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Za-z0-9-]{1,64}$");

	private final String sku;
	private String name;
	private String description;
	private String brand;
	private String category;
	private Map<String, Object> attributes;
	private Price price;

	public Product(String sku, String name, String description, String brand, String category,
			Map<String, Object> attributes, Price price) {
		if (sku == null || !SKU_PATTERN.matcher(sku).matches()) {
			throw new IllegalArgumentException("sku must be 1-64 letters, digits or dashes");
		}
		this.sku = sku;
		this.price = price;
		replaceDetails(name, description, brand, category, attributes);
	}

	public static Product create(String sku, String name, String description, String brand, String category,
			Map<String, Object> attributes) {
		return new Product(sku, name, description, brand, category, attributes, null);
	}

	public void replaceDetails(String name, String description, String brand, String category,
			Map<String, Object> attributes) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("name is required");
		}
		this.name = name;
		this.description = description;
		this.brand = brand;
		this.category = category;
		this.attributes = validAttributes(attributes);
	}

	public boolean applyPrice(Price newPrice) {
		if (newPrice == null) {
			throw new IllegalArgumentException("price is required");
		}
		if (price != null && !newPrice.isNewerThan(price)) {
			return false;
		}
		price = newPrice;
		return true;
	}

	private static Map<String, Object> validAttributes(Map<String, Object> attributes) {
		if (attributes == null) {
			return Map.of();
		}
		attributes.forEach((key, value) -> {
			if (!(value instanceof String || value instanceof Number || value instanceof Boolean)) {
				throw new IllegalArgumentException("attribute '" + key + "' must be text, a number or true/false");
			}
		});
		return Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
	}

	public String getSku() {
		return sku;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getBrand() {
		return brand;
	}

	public String getCategory() {
		return category;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Optional<Price> getPrice() {
		return Optional.ofNullable(price);
	}

}
