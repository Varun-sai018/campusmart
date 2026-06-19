package com.campusmart.review.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
class ReviewControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createUpdateDeleteReviewFlow() throws Exception {
        AuthContext buyer = register("review.buyer@example.com", "BUYER");
        AuthContext seller = register("review.seller@example.com", "SELLER");
        Long categoryId = getFirstCategoryId();
        Long productId = createProduct(seller.token(), seller.userId(), categoryId);

        addProductToCart(productId, buyer.token());
        placeOrder(buyer.token());

        MvcResult createResult = mockMvc.perform(post("/api/products/{productId}/reviews", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"rating\": 5," +
                                "\"comment\": \"Excellent product\"" +
                                "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.rating", is(5)))
                .andReturn();

        Long reviewId = ((Number) JsonPath.read(createResult.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(put("/api/reviews/{reviewId}", reviewId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"rating\": 4," +
                                "\"comment\": \"Good product\"" +
                                "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating", is(4)))
                .andExpect(jsonPath("$.comment", is("Good product")));

        mockMvc.perform(get("/api/products/{productId}/reviews", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/api/products/{productId}/rating-summary", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating", is(4.0)))
                .andExpect(jsonPath("$.totalReviews", is(1)))
                .andExpect(jsonPath("$.ratingBreakdown.4", is(1)));

        mockMvc.perform(delete("/api/reviews/{reviewId}", reviewId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isNoContent());
    }

    @Test
    void createReviewReturnsForbiddenWhenBuyerDidNotPurchase() throws Exception {
        AuthContext buyer = register("review.nobuy@example.com", "BUYER");
        AuthContext seller = register("review.owner@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(post("/api/products/{productId}/reviews", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"rating\": 5," +
                                "\"comment\": \"Attempted review\"" +
                                "}"))
                .andExpect(status().isForbidden());
    }

    private void addProductToCart(Long productId, String token) throws Exception {
        mockMvc.perform(post("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isOk());
    }

    private void placeOrder(String token) throws Exception {
        mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isCreated());
    }

    private Long createProduct(String token, Long sellerId, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\": \"Review Product\"," +
                                "\"description\": \"Product for review flow\"," +
                                "\"price\": 29.99," +
                                "\"condition\": \"NEW\"," +
                                "\"sellerId\": " + sellerId + "," +
                                "\"categoryId\": " + categoryId +
                                "}"))
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
                        .content("{" +
                                "\"firstName\": \"Review\"," +
                                "\"lastName\": \"Tester\"," +
                                "\"email\": \"" + email + "\"," +
                                "\"password\": \"Password123\"," +
                                "\"roles\": [\"" + role + "\"]" +
                                "}"))
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
