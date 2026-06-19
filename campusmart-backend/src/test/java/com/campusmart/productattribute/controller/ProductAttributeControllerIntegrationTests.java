package com.campusmart.productattribute.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductAttributeControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAndGetAttributesForProduct() throws Exception {
        AuthContext seller = register("seller.attr@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/products/{productId}/attributes", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attributeName\": \"Color\", \"attributeValue\": \"Blue\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.attributeName").value("Color"))
                .andExpect(jsonPath("$.attributeValue").value("Blue"))
                .andExpect(jsonPath("$.productId").value(productId));

        mockMvc.perform(get("/api/products/{productId}/attributes", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].attributeName").value("Color"));
    }

    @Test
    void createAttributeRejectsMissingFields() throws Exception {
        AuthContext seller = register("seller.attr.invalid@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/products/{productId}/attributes", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attributeName\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void deleteAttributeByOwnerAndViewEmpty() throws Exception {
        AuthContext seller = register("seller.attr.delete@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        MvcResult result = mockMvc.perform(post("/api/products/{productId}/attributes", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attributeName\": \"Color\", \"attributeValue\": \"Blue\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        Integer attributeId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/products/attributes/{attributeId}", attributeId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{productId}/attributes", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void buyerCannotCreateOrDeleteAttributes() throws Exception {
        AuthContext seller = register("seller.attr.auth@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());
        AuthContext buyer = register("buyer.attr@example.com", "BUYER");

        mockMvc.perform(post("/api/products/{productId}/attributes", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attributeName\": \"Color\", \"attributeValue\": \"Blue\"}"))
                .andExpect(status().isForbidden());

        MvcResult result = mockMvc.perform(post("/api/products/{productId}/attributes", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"attributeName\": \"Color\", \"attributeValue\": \"Blue\"}"))
                .andExpect(status().isCreated())
                .andReturn();

        Integer attributeId = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/products/attributes/{attributeId}", attributeId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isForbidden());
    }

    private Long createProduct(String token, Long sellerId, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test Product",
                                  "description": "Product attribute test",
                                  "price": 19.99,
                                  "condition": "NEW",
                                  "sellerId": %d,
                                  "categoryId": %d
                                }
                                """.formatted(sellerId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn();

        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();
    }

    private Long getFirstCategoryId() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/categories/active"))
                .andExpect(status().isOk())
                .andReturn();

        return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$[0].id")).longValue();
    }

    private AuthContext register(String email, String role) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "Attr",
                                  "lastName": "Tester",
                                  "email": "%s",
                                  "password": "Password123",
                                  "roles": ["%s"]
                                }
                                """.formatted(email, role)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return new AuthContext(
                JsonPath.read(body, "$.token"),
                ((Number) JsonPath.read(body, "$.userId")).longValue()
        );
    }

    private record AuthContext(String token, Long userId) {
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
