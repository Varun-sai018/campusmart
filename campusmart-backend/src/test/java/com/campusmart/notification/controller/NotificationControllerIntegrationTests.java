package com.campusmart.notification.controller;

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
class NotificationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void orderPlacementCreatesNotificationsForBuyerAndSeller() throws Exception {
        AuthContext buyer = register("notify.buyer@example.com", "BUYER");
        AuthContext seller = register("notify.seller@example.com", "SELLER");
        Long categoryId = getFirstCategoryId();
        Long productId = createProduct(seller.token(), seller.userId(), categoryId);

        mockMvc.perform(post("/api/cart/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 1}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("ORDER_PLACED")))
                .andExpect(jsonPath("$[0].message", is("Your order 1 has been placed successfully.")));

        mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type", is("PRODUCT_RESERVED")));
    }

    @Test
    void wishlistProductSoldNotifiesWishlistUsersAndCanMarkRead() throws Exception {
        AuthContext buyer = register("notifywishlist.buyer@example.com", "BUYER");
        AuthContext seller = register("notifywishlist.seller@example.com", "SELLER");
        Long categoryId = getFirstCategoryId();
        Long productId = createProduct(seller.token(), seller.userId(), categoryId);

        mockMvc.perform(post("/api/wishlist/{productId}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/products/{id}", productId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\": \"Wishlist Product\"," +
                                "\"description\": \"Wishlist flow product\"," +
                                "\"price\": 49.99," +
                                "\"condition\": \"NEW\"," +
                                "\"status\": \"SOLD\"," +
                                "\"categoryId\": " + categoryId +
                                "}"))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/notifications")
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type", is("WISHLIST_PRODUCT_SOLD")))
                .andExpect(jsonPath("$[1].type", is("WISHLIST_ADDED")))
                .andReturn();

        Long notificationId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$[0].id")).longValue();

        mockMvc.perform(post("/api/notifications/{notificationId}/read", notificationId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isNoContent());
    }

    private Long createProduct(String token, Long sellerId, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"title\": \"Notification Product\"," +
                                "\"description\": \"Product for notification flows\"," +
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
                                "\"firstName\": \"Notifier\"," +
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
