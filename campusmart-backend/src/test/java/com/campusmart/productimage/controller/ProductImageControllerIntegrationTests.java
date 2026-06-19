package com.campusmart.productimage.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class ProductImageControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sellerCanUploadAndViewImages() throws Exception {
        AuthContext seller = register("seller.images@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        mockMvc.perform(multipart("/api/products/{productId}/images", productId)
                        .file(jpegFile("photo.jpg"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.primaryImage").value(true))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.imageUrl").value(org.hamcrest.Matchers.startsWith("/uploads/products/")));

        mockMvc.perform(get("/api/products/{productId}/images", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].primaryImage").value(true));
    }

    @Test
    void uploadRejectsInvalidFileType() throws Exception {
        AuthContext seller = register("invalid.type@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        MockMultipartFile textFile = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes()
        );

        mockMvc.perform(multipart("/api/products/{productId}/images", productId)
                        .file(textFile)
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Unsupported file type")));
    }

    @Test
    void uploadRejectsSixthImage() throws Exception {
        AuthContext seller = register("max.images@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(multipart("/api/products/{productId}/images", productId)
                            .file(jpegFile("photo-" + i + ".jpg"))
                            .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(multipart("/api/products/{productId}/images", productId)
                        .file(jpegFile("photo-6.jpg"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A product can have at most 5 images"));
    }

    @Test
    void buyerCannotUploadToOthersProduct() throws Exception {
        AuthContext seller = register("owner.upload@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        AuthContext buyer = register("buyer.upload@example.com", "BUYER");

        mockMvc.perform(multipart("/api/products/{productId}/images", productId)
                        .file(jpegFile("photo.jpg"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isForbidden());
    }

    @Test
    void ownerCanDeleteImageAndPromotesNextPrimary() throws Exception {
        AuthContext seller = register("delete.images@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        MvcResult firstUpload = mockMvc.perform(multipart("/api/products/{productId}/images", productId)
                        .file(jpegFile("first.jpg"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isCreated())
                .andReturn();
        Integer firstImageId = JsonPath.read(firstUpload.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(multipart("/api/products/{productId}/images", productId)
                        .file(jpegFile("second.jpg"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.primaryImage").value(false));

        mockMvc.perform(delete("/api/products/images/{imageId}", firstImageId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{productId}/images", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].primaryImage").value(true));
    }

    @Test
    void buyerCannotDeleteImage() throws Exception {
        AuthContext seller = register("owner.delete@example.com", "SELLER");
        Long productId = createProduct(seller.token(), seller.userId(), getFirstCategoryId());

        MvcResult upload = mockMvc.perform(multipart("/api/products/{productId}/images", productId)
                        .file(jpegFile("photo.jpg"))
                        .header(HttpHeaders.AUTHORIZATION, bearer(seller.token())))
                .andExpect(status().isCreated())
                .andReturn();
        Integer imageId = JsonPath.read(upload.getResponse().getContentAsString(), "$.id");

        AuthContext buyer = register("buyer.delete@example.com", "BUYER");

        mockMvc.perform(delete("/api/products/images/{imageId}", imageId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(buyer.token())))
                .andExpect(status().isForbidden());
    }

    private Long createProduct(String sellerToken, Long sellerId, Long categoryId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .header(HttpHeaders.AUTHORIZATION, bearer(sellerToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Test Product",
                                  "description": "For image tests",
                                  "price": 25.00,
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
                                  "firstName": "Image",
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

    private MockMultipartFile jpegFile(String filename) {
        return new MockMultipartFile("file", filename, "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, 0, 0});
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
