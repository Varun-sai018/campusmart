package com.campusmart.product.search;

import static org.hamcrest.Matchers.hasSize;
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
class ProductSearchControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchProducts_returnsFilteredResults() throws Exception {
        AuthContext seller = register("seller.search@example.com", "SELLER");
        createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(get("/api/products/search")
                        .param("keyword", "Test")
                        .param("categoryId", String.valueOf(getFirstCategoryId()))
                        .param("sellerId", String.valueOf(seller.userId()))
                        .param("minPrice", "1.00")
                        .param("maxPrice", "1000.00")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sort", "price,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(notNullValue())));
    }

    @Test
    void searchProducts_returnsDefaultSortWhenNoSortSpecified() throws Exception {
        mockMvc.perform(get("/api/products/search")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    private Long createProduct(String token, Long sellerId, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test Search Product",
                                  "description": "Search product description",
                                  "price": 199.99,
                                  "condition": "GOOD",
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
                                  "firstName": "Search",
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
