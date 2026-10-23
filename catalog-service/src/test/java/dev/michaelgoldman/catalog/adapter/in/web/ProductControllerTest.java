package dev.michaelgoldman.catalog.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.michaelgoldman.catalog.application.ProductService;
import dev.michaelgoldman.catalog.application.UpsertResult;
import dev.michaelgoldman.catalog.domain.Product;

import java.util.Map;

import dev.michaelgoldman.catalog.domain.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

// only the web part of spring: controller, mapper, jackson and validation, the service is a mock
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class ProductControllerTest {

    private static final String SKU = "ADIDAS-SHOE-42";
    private static final String BODY = """
            { "name": "Running shoe", "attributes": { "size": 42 } }
            """;

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ProductService productService;

    @Test
    void upsertProduct_newSku_returns201() throws Exception {
        when(productService.upsert(any())).thenReturn(new UpsertResult(product(), true));

        mockMvc.perform(put("/products/{sku}", SKU).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(SKU))
                .andExpect(jsonPath("$.name").value("Running shoe"))
                .andExpect(jsonPath("$.price").doesNotHaveJsonPath());
    }

    @Test
    void upsertProduct_existingSku_returns200() throws Exception {
        when(productService.upsert(any())).thenReturn(new UpsertResult(product(), false));

        mockMvc.perform(put("/products/{sku}", SKU).contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isOk());
    }

    @Test
    void upsertProduct_missingName_returns400() throws Exception {
        mockMvc.perform(put("/products/{sku}", SKU).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void getProduct_exists_returns200() throws Exception {
        when(productService.get(any())).thenReturn(product());

        mockMvc.perform(get("/products/{sku}", SKU))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value(SKU));
    }

    @Test
    void getProduct_doesNotExist_returns404() throws Exception {
        when(productService.get(any())).thenThrow(ProductNotFoundException.class);

        mockMvc.perform(get("/products/{sku}", SKU))
                .andExpect(status().isNotFound());
    }

    private static Product product() {
        return Product.create(SKU, "Running shoe", null, null, null, Map.of("size", 42));
    }
}
