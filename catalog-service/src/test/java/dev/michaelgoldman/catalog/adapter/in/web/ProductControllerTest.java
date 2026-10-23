package dev.michaelgoldman.catalog.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.michaelgoldman.catalog.application.ProductPage;
import dev.michaelgoldman.catalog.application.ProductService;
import dev.michaelgoldman.catalog.application.UpsertResult;
import dev.michaelgoldman.catalog.domain.Product;

import java.util.List;
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
                .andExpect(status().isBadRequest())
                // same error format as the contract promises (Problem, RFC 9457)
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        verifyNoInteractions(productService);
    }

    @Test
    void upsertProduct_invalidSku_returns400() throws Exception {
        // an underscore isn't allowed in a sku (contract pattern)
        mockMvc.perform(put("/products/{sku}", "BAD_SKU").contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

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

    @Test
    void listProducts_returnsOnePage() throws Exception {
        // no page or size in the url, so the contract's defaults apply: page 0, size 20
        when(productService.search(null, 0, 20)).thenReturn(new ProductPage(List.of(product()), 0, 20, 1));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value(SKU))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void listProducts_withQ_searches() throws Exception {
        when(productService.search("red sho", 0, 20)).thenReturn(new ProductPage(List.of(product()), 0, 20, 1));

        mockMvc.perform(get("/products").param("q", "red sho"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value(SKU));
    }

    private static Product product() {
        return Product.create(SKU, "Running shoe", null, null, null, Map.of("size", 42));
    }
}
