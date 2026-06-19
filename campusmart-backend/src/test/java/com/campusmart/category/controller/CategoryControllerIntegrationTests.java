package com.campusmart.category.controller;

import static org.hamcrest.Matchers.hasItem;
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
class CategoryControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCategoriesIsPublicAndReturnsDefaultCategories() throws Exception {
        mockMvc.perform(get("/api/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", hasItem("BOOKS")))
                .andExpect(jsonPath("$[*].name", hasItem("ELECTRONICS")))
                .andExpect(jsonPath("$[*].name", hasItem("LAB_EQUIPMENT")));
    }

    @Test
    void adminCanCreateUpdateAndDeleteCategory() throws Exception {
        String token = registerAndGetToken("admin.category@example.com", "ADMIN");

        MvcResult createResult = mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "sports_gear",
                                  "description": "Sports gear"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("SPORTS_GEAR"))
                .andExpect(jsonPath("$.isActive").value(true))
                .andReturn();

        Integer categoryId = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(put("/api/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "sports_equipment",
                                  "description": "Updated sports equipment",
                                  "isActive": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("SPORTS_EQUIPMENT"));

        mockMvc.perform(delete("/api/categories/{id}", categoryId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/categories/{id}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isActive").value(false));
    }

    @Test
    void buyerCannotCreateCategory() throws Exception {
        String token = registerAndGetToken("buyer.category@example.com", "BUYER");

        mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "unauthorized_category"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCategoryRejectsDuplicateName() throws Exception {
        String token = registerAndGetToken("duplicate.category@example.com", "ADMIN");

        mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "books"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Category already exists with name: BOOKS"));
    }

    @Test
    void createCategoryValidatesName() throws Exception {
        String token = registerAndGetToken("validation.category@example.com", "ADMIN");

        mockMvc.perform(post("/api/categories")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Missing name"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name", notNullValue()));
    }

    private String registerAndGetToken(String email, String role) throws Exception {
        String request = """
                {
                  "firstName": "Category",
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

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}

