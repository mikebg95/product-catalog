package dev.michaelgoldman.fakeprice.web;

import dev.michaelgoldman.fakeprice.api.PricesApi;
import dev.michaelgoldman.fakeprice.api.SkusApi;
import dev.michaelgoldman.fakeprice.api.model.PriceDto;
import dev.michaelgoldman.fakeprice.api.model.PriceListDto;
import dev.michaelgoldman.fakeprice.api.model.PriceRequestDto;
import dev.michaelgoldman.fakeprice.api.model.SkuDto;
import dev.michaelgoldman.fakeprice.api.model.SkuListDto;
import dev.michaelgoldman.fakeprice.api.model.SkuPriceDto;
import dev.michaelgoldman.fakeprice.store.PriceStore;
import dev.michaelgoldman.fakeprice.store.StoredPrice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@RestController
public class PriceController implements PricesApi, SkusApi {

    private final PriceStore store;

    public PriceController(PriceStore store) {
        this.store = store;
    }

    @Override
    public ResponseEntity<PriceListDto> listPrices() {
        return ResponseEntity.ok(toDto(store.getStoredPrices()));
    }

    @Override
    public ResponseEntity<SkuPriceDto> setPrice(String sku, PriceRequestDto priceRequestDto) {
        // the price system sets the time itself, so a caller can't send an old or wrong one
        StoredPrice price = new StoredPrice(priceRequestDto.getAmount(), Instant.now());

        if (!store.setPrice(sku, price)) {
            // only registered skus can get a price
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new SkuPriceDto(sku, toDto(price)));
    }

    @Override
    public ResponseEntity<SkuListDto> listSkus() {
        List<SkuDto> skus = store.listSkus().stream()
                .map(sku -> {
                    SkuDto skuDto = new SkuDto(sku);
                    StoredPrice price = store.findBySku(sku);
                    // a registered sku without a price has no price field
                    if (price != null) {
                        skuDto.price(toDto(price));
                    }
                    return skuDto;
                })
                .toList();

        return ResponseEntity.ok(new SkuListDto(skus));
    }

    @Override
    public ResponseEntity<Void> registerSku(String sku) {
        boolean newSku = store.registerSku(sku);
        if (newSku) {
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        return ResponseEntity.noContent().build();
    }

    private PriceListDto toDto(Map<String, StoredPrice> storedPrices) {
        return new PriceListDto(storedPrices.entrySet().stream()
                .map(entry -> new SkuPriceDto(entry.getKey(), toDto(entry.getValue())))
                .toList());
    }

    private PriceDto toDto(StoredPrice storedPrice) {
        return new PriceDto(storedPrice.amount(), PriceDto.CurrencyEnum.EUR, storedPrice.updatedAt().atOffset(ZoneOffset.UTC));
    }
}
