package com.campusmart.productimage.service;

import com.campusmart.config.FileStorageProperties;
import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ProductImageNotFoundException;
import com.campusmart.exception.ProductNotFoundException;
import com.campusmart.product.entity.Product;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.productimage.dto.ProductImageDto;
import com.campusmart.productimage.entity.ProductImage;
import com.campusmart.productimage.repository.ProductImageRepository;
import com.campusmart.storage.FileStorageService;
import com.campusmart.storage.StoredFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;
    private final FileStorageProperties fileStorageProperties;

    @Transactional
    public ProductImageDto uploadImage(Long productId, MultipartFile file, Long currentUserId, boolean admin) {
        Product product = getActiveProduct(productId);
        assertOwnerOrAdmin(product, currentUserId, admin);

        if (productImageRepository.countByProduct(product) >= fileStorageProperties.getMaxImagesPerProduct()) {
            throw new BadRequestException("A product can have at most 5 images");
        }

        StoredFile storedFile = fileStorageService.store(file, fileStorageProperties.getProductsSubdir());

        try {
            boolean primary = productImageRepository.countByProduct(product) == 0;
            ProductImage image = ProductImage.builder()
                    .imageUrl(storedFile.publicUrl())
                    .storageKey(storedFile.storageKey())
                    .primaryImage(primary)
                    .product(product)
                    .build();
            return toDto(productImageRepository.save(image));
        } catch (RuntimeException ex) {
            fileStorageService.delete(storedFile.storageKey());
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<ProductImageDto> getImagesByProduct(Long productId) {
        Product product = getActiveProduct(productId);
        return productImageRepository.findByProductOrderByCreatedAtAsc(product)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deleteImage(Long imageId, Long currentUserId, boolean admin) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ProductImageNotFoundException(imageId));

        Product product = image.getProduct();
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ProductNotFoundException(product.getId());
        }
        assertOwnerOrAdmin(product, currentUserId, admin);

        boolean wasPrimary = Boolean.TRUE.equals(image.getPrimaryImage());
        String storageKey = image.getStorageKey();

        productImageRepository.delete(image);
        fileStorageService.delete(storageKey);

        if (wasPrimary) {
            promoteOldestRemainingImage(product);
        }
    }

    private void promoteOldestRemainingImage(Product product) {
        List<ProductImage> remaining = productImageRepository.findByProductOrderByCreatedAtAsc(product);
        if (!remaining.isEmpty()) {
            ProductImage nextPrimary = remaining.get(0);
            nextPrimary.setPrimaryImage(true);
            productImageRepository.save(nextPrimary);
        }
    }

    private Product getActiveProduct(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> Boolean.TRUE.equals(product.getIsActive()))
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void assertOwnerOrAdmin(Product product, Long currentUserId, boolean admin) {
        if (!admin && !product.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the product owner or an admin can manage product images");
        }
    }

    private ProductImageDto toDto(ProductImage image) {
        return new ProductImageDto(
                image.getId(),
                image.getImageUrl(),
                image.getPrimaryImage(),
                image.getProduct().getId(),
                image.getCreatedAt()
        );
    }
}
