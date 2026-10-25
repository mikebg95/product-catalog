package dev.michaelgoldman.catalog.adapter.in.scheduling;

import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import dev.michaelgoldman.catalog.TestcontainersConfiguration;
import dev.michaelgoldman.catalog.application.PriceSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class PriceSyncLockTest {

    @MockitoBean
    PriceSyncService priceSyncService;

    @Autowired
    PriceSyncScheduler priceSyncScheduler;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void syncPrices_anotherInstanceHoldsTheLock_skipsTheRound() {
        // the scheduler runs once right after startup
        verify(priceSyncService, timeout(10_000)).syncPrices();
        jdbcTemplate.update("""
                UPDATE shedlock
                SET lock_until = timezone('utc', now()) + interval '1 hour', locked_by = 'other-instance'
                WHERE name = 'priceSync'
                """);
        clearInvocations(priceSyncService);

        priceSyncScheduler.syncPrices();

        verify(priceSyncService, never()).syncPrices();
    }
}
