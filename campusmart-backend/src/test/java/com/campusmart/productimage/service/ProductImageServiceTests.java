package com.campusmart.productimage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.campusmart.config.FileStorageProperties;
import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ProductImageNotFoundException;
import com.campusmart.product.entity.Product;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.productimage.entity.ProductImage;
import com.campusmart.productimage.repository.ProductImageRepository;
import com.campusmart.storage.FileStorageService;
import com.campusmart.storage.StoredFile;
import com.campusmart.user.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class ProductImageServiceTests {

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private FileStorageProperties fileStorageProperties;

    @InjectMocks
    private ProductImageService productImageService;

    private Product product;
    private MockMultipartFile imageFile;

    @BeforeEach
    void setUp() {
        User seller = User.builder().id(1L).firstName("Jane").lastName("Seller").build();
        product = Product.builder()
                .id(10L)
                .isActive(true)
                .seller(seller)
                .build();
        imageFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    private void stubStorageProperties() {
        when(fileStorageProperties.getMaxImagesPerProduct()).thenReturn(5);
        when(fileStorageProperties.getProductsSubdir()).thenReturn("products");
    }

    @Test
    void uploadImage_setsPrimaryOnFirstUpload() {
        stubStorageProperties();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productImageRepository.countByProduct(product)).thenReturn(0L);
        when(fileStorageService.store(any(), eq("products")))
                .thenReturn(new StoredFile("products/uuid.jpg", "/uploads/products/uuid.jpg"));
        when(productImageRepository.save(any(ProductImage.class))).thenAnswer(invocation -> {
            ProductImage saved = invocation.getArgument(0);
            saved.setId(100L);
            saved.setCreatedAt(LocalDateTime.now());
            return saved;
        });

        var response = productImageService.uploadImage(10L, imageFile, 1L, false);

        assertThat(response.primaryImage()).isTrue();
        assertThat(response.imageUrl()).isEqualTo("/uploads/products/uuid.jpg");
    }

    @Test
    void uploadImage_rejectsSixthImage() {
        when(fileStorageProperties.getMaxImagesPerProduct()).thenReturn(5);
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productImageRepository.countByProduct(product)).thenReturn(5L);

        assertThatThrownBy(() -> productImageService.uploadImage(10L, imageFile, 1L, false))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at most 5 images");
    }

    @Test
    void uploadImage_rejectsNonOwner() {
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productImageService.uploadImage(10L, imageFile, 99L, false))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deleteImage_promotesOldestWhenPrimaryDeleted() {
        ProductImage primary = image(1L, true, "products/a.jpg");
        ProductImage secondary = image(2L, false, "products/b.jpg");

        when(productImageRepository.findById(1L)).thenReturn(Optional.of(primary));
        when(productImageRepository.findByProductOrderByCreatedAtAsc(product)).thenReturn(List.of(secondary));

        productImageService.deleteImage(1L, 1L, false);

        verify(fileStorageService).delete("products/a.jpg");
        verify(productImageRepository).delete(primary);
        assertThat(secondary.getPrimaryImage()).isTrue();
        verify(productImageRepository).save(secondary);
    }

    @Test
    void deleteImage_rejectsNonOwner() {
        ProductImage image = image(1L, true, "products/a.jpg");
        when(productImageRepository.findById(1L)).thenReturn(Optional.of(image));

        assertThatThrownBy(() -> productImageService.deleteImage(1L, 99L, false))
                .isInstanceOf(AccessDeniedException.class);

        verify(productImageRepository, never()).delete(any());
    }

    @Test
    void deleteImage_throwsWhenMissing() {
        when(productImageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productImageService.deleteImage(99L, 1L, false))
                .isInstanceOf(ProductImageNotFoundException.class);
    }

    @Test
    void uploadImage_deletesStoredFileOnDbFailure() {
        stubStorageProperties();
        when(productRepository.findById(10L)).thenReturn(Optional.of(product));
        when(productImageRepository.countByProduct(product)).thenReturn(0L);
        when(fileStorageService.store(any(), eq("products")))
                .thenReturn(new StoredFile("products/uuid.jpg", "/uploads/products/uuid.jpg"));
        when(productImageRepository.save(any(ProductImage.class))).thenThrow(new RuntimeException("db error"));

        assertThatThrownBy(() -> productImageService.uploadImage(10L, imageFile, 1L, false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("db error");

        verify(fileStorageService).delete("products/uuid.jpg");
    }

    private ProductImage image(Long id, boolean primary, String storageKey) {
        return ProductImage.builder()
                .id(id)
                .primaryImage(primary)
                .storageKey(storageKey)
                .imageUrl("/uploads/" + storageKey)
                .product(product)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
