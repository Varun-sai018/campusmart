package com.campusmart.cart.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
class CartControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addToCart_createsOrUpdatesCartItem() throws Exception {
        AuthContext buyer = register("cart.buyer@example.com", "BUYER");
        AuthContext seller = register("cart.seller@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.quantity").value(2))
                .andExpect(jsonPath("$.productId").value(productId));

        mockMvc.perform(post("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void updateCartItem_changesQuantity() throws Exception {
        AuthContext buyer = register("cart.update@example.com", "BUYER");
        AuthContext seller = register("cart.update.seller@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(4));
    }

    @Test
    void getCart_returnsSummaryForUser() throws Exception {
        AuthContext buyer = register("cart.list@example.com", "BUYER");
        AuthContext seller = register("cart.list.seller@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 2}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cart")
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalQuantity").value(2))
                .andExpect(jsonPath("$.cartItems", hasSize(1)));
    }

    @Test
    void removeFromCart_deletesItem() throws Exception {
        AuthContext buyer = register("cart.delete@example.com", "BUYER");
        AuthContext seller = register("cart.delete.seller@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isNoContent());
    }

    @Test
    void clearCart_removesAllItems() throws Exception {
        AuthContext buyer = register("cart.clear@example.com", "BUYER");
        AuthContext seller = register("cart.clear.seller@example.com", "SELLER");
        Long firstProduct = createProduct(seller.token(), seller.userId(), getFirstCategoryId());
        Long secondProduct = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/cart/{productId}", firstProduct)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/cart/{productId}", secondProduct)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/cart")
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isNoContent());
    }

    private Long createProduct(String token, Long sellerId, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Cart Product",
                                  "description": "Cart product description",
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
                                  "firstName": "Cart",
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
