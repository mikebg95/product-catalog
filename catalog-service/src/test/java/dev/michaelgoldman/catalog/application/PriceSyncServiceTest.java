package dev.michaelgoldman.catalog.application;

import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.domain.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PriceSyncServiceTest {

    private static final String SKU = "ADIDAS-SHOE-42";
    private static final Instant BEFORE = Instant.parse("2026-10-21T12:00:00Z");
    private static final Instant NOW = Instant.parse("2026-10-22T12:00:00Z");
    private static final String AMOUNT_OLD = "89.95";
    private static final String AMOUNT_NEW = "60.00";

    private final InMemoryProductRepository repository = new InMemoryProductRepository();
    private final FakePriceSource priceSource = new FakePriceSource();

    private final PriceSyncService service = new PriceSyncService(repository, priceSource);

    @Test
    void syncPrices_whenPriceIsNewer_replacesPrice() {
        Price oldPrice = new Price(new BigDecimal(AMOUNT_OLD), BEFORE);
        Price newPrice = new Price(new BigDecimal(AMOUNT_NEW), NOW);
        repository.save(new Product(SKU, "Red shoe", null, null, null, Map.of(), oldPrice));
        priceSource.add(SKU, newPrice);

        service.syncPrices();

        Product saved = repository.findBySku(SKU).orElseThrow();
        assertThat(saved.getPrice()).contains(newPrice);
    }

    @Test
    void syncPrices_whenPriceIsNotNewer_keepsExistingPrice() {
        Price oldPrice = new Price(new BigDecimal(AMOUNT_OLD), BEFORE);
        Price newPrice = new Price(new BigDecimal(AMOUNT_NEW), NOW);
        repository.save(new Product(SKU, "Red shoe", null, null, null, Map.of(), newPrice));
        priceSource.add(SKU, oldPrice);

        service.syncPrices();

        Product saved = repository.findBySku(SKU).orElseThrow();
        assertThat(saved.getPrice()).contains(newPrice);
    }

    @Test
    void syncPrices_whenSkuIsUnknown_skipsItAndSyncsTheRest() {
        Price price = new Price(new BigDecimal(AMOUNT_NEW), NOW);
        repository.save(new Product(SKU, "Red shoe", null, null, null, Map.of(), null));
        priceSource.add("UNKNOWN-SKU", price);
        priceSource.add(SKU, price);

        service.syncPrices();

        Product saved = repository.findBySku(SKU).orElseThrow();
        assertThat(saved.getPrice()).contains(price);
        assertThat(repository.findBySku("UNKNOWN-SKU")).isEmpty();
    }

    @Test
    void syncPrices_whenSourceReturnsNothing_changesNoPrices() {
        Price oldPrice = new Price(new BigDecimal(AMOUNT_OLD), BEFORE);
        repository.save(new Product(SKU, "Red shoe", null, null, null, Map.of(), oldPrice));

        service.syncPrices();

        Product saved = repository.findBySku(SKU).orElseThrow();
        assertThat(saved.getPrice()).contains(oldPrice);
    }
}
