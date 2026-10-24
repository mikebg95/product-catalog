package dev.michaelgoldman.fakeprice.demo;

import dev.michaelgoldman.fakeprice.store.PriceStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

// demo only, not part of price-api.yaml: turns the price api off and on, for the outage demo in fake-price-ui
@RestController
@RequestMapping("/demo/outage")
public class DemoController {
    private final OutageSwitch outageSwitch;
    private final PriceStore store;

    public DemoController(OutageSwitch outageSwitch, PriceStore store) {
        this.outageSwitch = outageSwitch;
        this.store = store;
    }

    public record OutageStatus(boolean down, Instant downSince) {
    }

    @GetMapping
    public OutageStatus status() {
        return new OutageStatus(outageSwitch.isDown(), outageSwitch.downSince());
    }

    @PostMapping("/off")
    public OutageStatus turnOff() {
        outageSwitch.turnOff();
        return status();
    }

    // comes back empty, like a real restart of a system that keeps its data in memory
    @PostMapping("/on")
    public OutageStatus turnOn() {
        if (outageSwitch.isDown()) {
            store.clear();
        }
        outageSwitch.turnOn();
        return status();
    }
}
