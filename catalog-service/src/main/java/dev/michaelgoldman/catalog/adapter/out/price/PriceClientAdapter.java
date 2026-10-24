package dev.michaelgoldman.catalog.adapter.out.price;

import dev.michaelgoldman.catalog.application.port.PriceSource;
import dev.michaelgoldman.catalog.application.port.PriceSourceUnavailableException;
import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.priceclient.api.PricesApi;
import dev.michaelgoldman.catalog.priceclient.model.PriceDto;
import dev.michaelgoldman.catalog.priceclient.model.SkuPriceDto;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PriceClientAdapter implements PriceSource {

    private final PricesApi pricesApi;
    private final RetryTemplate retryTemplate;

    public PriceClientAdapter(PricesApi pricesApi, RetryTemplate retryTemplate) {
        this.pricesApi = pricesApi;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public Map<String, Price> fetchPrices() {
        List<SkuPriceDto> prices;
        try {
            // tries again on a temporary failure; which ones and how often is set in PriceClientConfig
            prices = retryTemplate.execute(() -> pricesApi.listPrices().getPrices());
        }
        // given up: every try failed, or the failure was not worth retrying (4xx)
        catch (RetryException e) {
            throw new PriceSourceUnavailableException("Could not fetch prices from the price system.", e.getLastException());
        }

        Map<String, Price> result = new HashMap<>();
        for (SkuPriceDto price : prices) {
            String sku = price.getSku();
            PriceDto priceDto = price.getPrice();

            result.put(sku, toDomain(priceDto));
        }

        return result;
    }

    private Price toDomain(PriceDto dto) {
        return new Price(dto.getAmount(), dto.getUpdatedAt().toInstant());
    }
}
