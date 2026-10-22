package dev.michaelgoldman.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProductTest {

	private static final Instant NOW = Instant.parse("2026-10-18T12:00:00Z");

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "SHOE 42", "SHOE/42", "SHOE_42" })
	void invalidSku_throws(String sku) {
		assertThatThrownBy(() -> Product.create(sku, "Running shoe", null, null, null, Map.of()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " " })
	void blankName_throws(String name) {
		assertThatThrownBy(() -> Product.create("SHOE-42", name, null, null, null, Map.of()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void nestedAttributeValue_throws() {
		assertThatThrownBy(() -> Product.create("SHOE-42", "Running shoe", null, null, null,
				Map.of("sizes", List.of(41, 42))))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> Product.create("SHOE-42", "Running shoe", null, null, null,
				Map.of("dimensions", Map.of("width", 10))))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void replaceDetails_keepsPrice() {
		Price price = price("89.95", NOW);
		Product product = new Product("SHOE-42", "Running shoe", null, null, null, Map.of(), price);

		product.replaceDetails("Trail shoe", "For rough terrain", "Acme", "Shoes", Map.of("color", "green"));

		assertThat(product.getName()).isEqualTo("Trail shoe");
		assertThat(product.getPrice()).contains(price);
	}

	@Test
	void applyPrice_appliesWhenNoPriceYet() {
		Product product = Product.create("SHOE-42", "Running shoe", null, null, null, Map.of());
		Price price = price("89.95", NOW);

		assertThat(product.applyPrice(price)).isTrue();
		assertThat(product.getPrice()).contains(price);
	}

	@Test
	void applyPrice_appliesWhenNewer() {
		Product product = new Product("SHOE-42", "Running shoe", null, null, null, Map.of(), price("89.95", NOW));
		Price newer = price("79.95", NOW.plusSeconds(1));

		assertThat(product.applyPrice(newer)).isTrue();
		assertThat(product.getPrice()).contains(newer);
	}

	@ParameterizedTest
	@ValueSource(longs = { 0, -1 })
	void applyPrice_ignoresWhenEqualOrOlder(long secondsFromNow) {
		Price current = price("89.95", NOW);
		Product product = new Product("SHOE-42", "Running shoe", null, null, null, Map.of(), current);

		assertThat(product.applyPrice(price("79.95", NOW.plusSeconds(secondsFromNow)))).isFalse();
		assertThat(product.getPrice()).contains(current);
	}

	private static Price price(String amount, Instant updatedAt) {
		return new Price(new BigDecimal(amount), updatedAt);
	}

}
