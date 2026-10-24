package dev.michaelgoldman.fakeprice.store;

import java.math.BigDecimal;
import java.time.Instant;

public record StoredPrice(BigDecimal amount, Instant updatedAt) {
}
