package com.campusmart.wishlist.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
class WishlistControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void addToWishlist_createsNewWishlistItem() throws Exception {
        AuthContext buyer = registerAndGetAuthContext("wishlist.user@example.com", "BUYER");
        AuthContext seller = registerAndGetAuthContext("wishlist.seller@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/wishlist/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.productId").value(productId));
    }

    @Test
    void getWishlist_returnsWishlistItemsForUser() throws Exception {
        AuthContext buyer = registerAndGetAuthContext("wishlist.list@example.com", "BUYER");
        AuthContext seller = registerAndGetAuthContext("wishlist.list.seller@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());
        mockMvc.perform(post("/api/wishlist/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/wishlist")
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId").value(productId));
    }

    @Test
    void removeFromWishlist_deletesWishlistItem() throws Exception {
        AuthContext buyer = registerAndGetAuthContext("wishlist.delete@example.com", "BUYER");
        AuthContext seller = registerAndGetAuthContext("wishlist.delete.seller@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());
        mockMvc.perform(post("/api/wishlist/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/wishlist/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token)))
                .andExpect(status().isNoContent());
    }

    private Long createProduct(String token, Long sellerId, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Wishlist Product",
                                  "description": "Wishlist product description",
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

    private AuthContext registerAndGetAuthContext(String email, String role) throws Exception {
        String request = """
                {
                  "firstName": "Wishlist",
                  "lastName": "Tester",
                  "email": "%s",
                  "password": "Password123",
                  "roles": ["%s"]
                }
                """.formatted(email, role);

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
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
