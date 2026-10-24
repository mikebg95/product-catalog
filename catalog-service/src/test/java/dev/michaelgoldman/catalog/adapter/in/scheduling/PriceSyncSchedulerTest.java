package dev.michaelgoldman.catalog.adapter.in.scheduling;

import dev.michaelgoldman.catalog.application.InMemoryProductRepository;
import dev.michaelgoldman.catalog.application.PriceSyncService;
import dev.michaelgoldman.catalog.application.port.PriceSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;

class PriceSyncSchedulerTest {

    private final InMemoryProductRepository repository = new InMemoryProductRepository();
    private final PriceSource failingSource = new FailingPriceSource();
    private final PriceSyncService priceSyncService = new PriceSyncService(repository, failingSource);
    private final PriceSyncScheduler priceSyncScheduler = new PriceSyncScheduler(priceSyncService);

    @Test
    void syncPrices_priceSourceThrowsException_doesNotThrow() {
        assertThatNoException().isThrownBy(priceSyncScheduler::syncPrices);
    }
}
