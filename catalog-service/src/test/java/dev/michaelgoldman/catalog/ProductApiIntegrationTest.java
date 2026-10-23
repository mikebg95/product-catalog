package dev.michaelgoldman.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

// the whole app with a real postgres: controller, service, adapter and database together
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ProductApiIntegrationTest {

    private static final String SKU = "E2E-SHOE-42";

    @Autowired
    MockMvc mockMvc;

    @Test
    void savedProductCanBeReadBack() throws Exception {
        mockMvc.perform(put("/products/{sku}", SKU).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Running shoe", "attributes": { "size": 42 } }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/products/{sku}", SKU).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Trail shoe", "attributes": { "size": 42 } }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/products/{sku}", SKU))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Trail shoe"))
                .andExpect(jsonPath("$.attributes.size").value(42))
                .andExpect(jsonPath("$.price").doesNotHaveJsonPath());
    }
}
