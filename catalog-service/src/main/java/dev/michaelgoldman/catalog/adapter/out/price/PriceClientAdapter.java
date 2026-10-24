package dev.michaelgoldman.catalog.adapter.out.price;

import dev.michaelgoldman.catalog.application.port.PriceSource;
import dev.michaelgoldman.catalog.application.port.PriceSourceUnavailableException;
import dev.michaelgoldman.catalog.domain.Price;
import dev.michaelgoldman.catalog.priceclient.api.PricesApi;
import dev.michaelgoldman.catalog.priceclient.model.PriceDto;
import dev.michaelgoldman.catalog.priceclient.model.SkuPriceDto;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PriceClientAdapter implements PriceSource {

    private final PricesApi pricesApi;

    public PriceClientAdapter(PricesApi pricesApi) {
        this.pricesApi = pricesApi;
    }

    @Override
    public Map<String, Price> fetchPrices() {
        List<SkuPriceDto> prices;
        try {
            prices = pricesApi.listPrices().getPrices();
        }
        // error answer (4xx/5xx) as well as not reachable or too slow
        catch (RestClientException e) {
            throw new PriceSourceUnavailableException("Could not fetch prices from the price system.", e);
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
