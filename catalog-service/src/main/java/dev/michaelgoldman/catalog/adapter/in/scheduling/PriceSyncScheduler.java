package dev.michaelgoldman.catalog.adapter.in.scheduling;

import dev.michaelgoldman.catalog.application.PriceSyncService;
import dev.michaelgoldman.catalog.application.port.PriceSourceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class PriceSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceSyncScheduler.class);

    private final PriceSyncService priceSyncService;

    public PriceSyncScheduler(PriceSyncService priceSyncService) {
        this.priceSyncService = priceSyncService;
    }

    @Scheduled(fixedDelay = 10_000)
    public void syncPrices() {
        try {
            priceSyncService.syncPrices();
        }
        catch (PriceSourceUnavailableException e) {
            log.warn("Price sync skipped this round: {} ({})", e.getMessage(), String.valueOf(e.getCause()));
        }
    }
}
