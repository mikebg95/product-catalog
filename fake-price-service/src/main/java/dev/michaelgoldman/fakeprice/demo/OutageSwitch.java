package dev.michaelgoldman.fakeprice.demo;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

// demo only: whether the price api is "down", and since when (null = up)
@Component
public class OutageSwitch {
    private final AtomicReference<Instant> downSince = new AtomicReference<>();

    public boolean isDown() {
        return downSince.get() != null;
    }

    public Instant downSince() {
        return downSince.get();
    }

    public void turnOff() {
        downSince.compareAndSet(null, Instant.now());
    }

    public void turnOn() {
        downSince.set(null);
    }
}
