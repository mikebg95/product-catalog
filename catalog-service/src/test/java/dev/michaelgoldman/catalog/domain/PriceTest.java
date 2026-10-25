package dev.michaelgoldman.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PriceTest {

	private static final Instant NOW = Instant.parse("2026-10-18T12:00:00Z");

	@ParameterizedTest
	@CsvSource({ "20, 20.00", "19.9, 19.90", "19.99, 19.99" })
	void validPrice_isCreatedWithTwoDecimals(String amount, String expected) {
		Price price = new Price(new BigDecimal(amount), NOW);

		assertThat(price.amount()).isEqualTo(new BigDecimal(expected));
		assertThat(price.updatedAt()).isEqualTo(NOW);
	}

	@ParameterizedTest
	@ValueSource(strings = { "0", "-1" })
	void zeroOrNegativeAmount_throws(String amount) {
		assertThatThrownBy(() -> new Price(new BigDecimal(amount), NOW))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void moreThanTwoDecimals_throws() {
		assertThatThrownBy(() -> new Price(new BigDecimal("19.999"), NOW))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void isNewerThan_onlyWhenLaterAndNotEqual() {
		Price current = new Price(new BigDecimal("19.99"), NOW);
		Price later = new Price(new BigDecimal("17.99"), NOW.plusSeconds(1));
		Price sameTime = new Price(new BigDecimal("17.99"), NOW);

		assertThat(later.isNewerThan(current)).isTrue();
		assertThat(sameTime.isNewerThan(current)).isFalse();
	}

	@Test
	void isNewerThan_ignoresDifferenceBelowMicroseconds() {
		Price stored = new Price(new BigDecimal("19.99"), NOW);
		Price incoming = new Price(new BigDecimal("19.99"), NOW.plusNanos(123));

		assertThat(incoming.isNewerThan(stored)).isFalse();
	}
}
