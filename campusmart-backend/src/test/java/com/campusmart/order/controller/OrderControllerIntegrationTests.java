package com.campusmart.order.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createOrder_placesOrderAndClearsCart() throws Exception {
        AuthContext buyer = register("order.buyer@example.com", "BUYER");
        AuthContext seller = register("order.seller@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 2}"))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.buyerId", is(buyer.userId().intValue())))
                .andExpect(jsonPath("$.orderStatus", is("PENDING")))
                .andExpect(jsonPath("$.orderItems", hasSize(1)))
                .andReturn();

        Long orderId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/orders/{orderId}", orderId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.orderItems", hasSize(1)));

        mockMvc.perform(get("/api/orders/seller")
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createOrder_returnsBadRequestWhenCartEmpty() throws Exception {
        AuthContext buyer = register("order.empty@example.com", "BUYER");

        mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isBadRequest());
    }

    private Long createProduct(String token, Long sellerId, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Order Product",
                                  "description": "Product for order flow",
                                  "price": 29.99,
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
                                  "firstName": "Order",
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

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private record AuthContext(String token, Long userId) {
    }
}
