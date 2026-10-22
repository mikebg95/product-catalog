package dev.michaelgoldman.catalog.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Price(BigDecimal amount, Instant updatedAt) {

	public Price {
		if (amount == null) {
			throw new IllegalArgumentException("amount is required");
		}
		if (updatedAt == null) {
			throw new IllegalArgumentException("updatedAt is required");
		}
		if (amount.signum() <= 0) {
			throw new IllegalArgumentException("amount must be greater than 0");
		}
		if (amount.stripTrailingZeros().scale() > 2) {
			throw new IllegalArgumentException("amount must have at most 2 decimals");
		}
		amount = amount.setScale(2);
	}

	public boolean isNewerThan(Price other) {
		return updatedAt.isAfter(other.updatedAt);
	}

}
