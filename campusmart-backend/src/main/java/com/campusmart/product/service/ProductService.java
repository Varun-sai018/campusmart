package com.campusmart.product.service;

import com.campusmart.category.entity.Category;
import com.campusmart.category.repository.CategoryRepository;
import com.campusmart.exception.CategoryNotFoundException;
import com.campusmart.exception.ProductNotFoundException;
import com.campusmart.exception.ResourceNotFoundException;
import com.campusmart.product.dto.ProductCreateRequestDto;
import com.campusmart.product.dto.ProductResponseDto;
import com.campusmart.product.dto.ProductUpdateRequestDto;
import com.campusmart.product.entity.Product;
import com.campusmart.product.entity.ProductStatus;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.user.entity.User;
import com.campusmart.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponseDto createProduct(ProductCreateRequestDto request, Long currentUserId) {
        if (!request.sellerId().equals(currentUserId)) {
            throw new AccessDeniedException("Sellers can create products only for their own account");
        }

        User seller = userRepository.findById(request.sellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + request.sellerId()));
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        Product product = Product.builder()
                .title(normalizeText(request.title()))
                .description(normalizeOptionalText(request.description()))
                .price(request.price())
                .condition(request.condition())
                .status(ProductStatus.AVAILABLE)
                .seller(seller)
                .category(category)
                .isActive(true)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        return productRepository.findById(id)
                .filter(product -> Boolean.TRUE.equals(product.getIsActive()))
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(Pageable pageable) {
        return productRepository.findByIsActiveTrue(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        return productRepository.findByCategoryAndIsActiveTrue(category, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getProductsBySeller(Long sellerId, Pageable pageable) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found with id: " + sellerId));
        return productRepository.findBySellerAndIsActiveTrue(seller, pageable).map(this::toResponse);
    }

    @Transactional
    public ProductResponseDto updateProduct(
            Long id,
            ProductUpdateRequestDto request,
            Long currentUserId,
            boolean admin
    ) {
        Product product = productRepository.findById(id)
                .filter(existing -> Boolean.TRUE.equals(existing.getIsActive()))
                .orElseThrow(() -> new ProductNotFoundException(id));
        assertOwnerOrAdmin(product, currentUserId, admin);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(request.categoryId()));

        product.setTitle(normalizeText(request.title()));
        product.setDescription(normalizeOptionalText(request.description()));
        product.setPrice(request.price());
        product.setCondition(request.condition());
        product.setStatus(request.status());
        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id, Long currentUserId, boolean admin) {
        Product product = productRepository.findById(id)
                .filter(existing -> Boolean.TRUE.equals(existing.getIsActive()))
                .orElseThrow(() -> new ProductNotFoundException(id));
        assertOwnerOrAdmin(product, currentUserId, admin);

        product.setIsActive(false);
        productRepository.save(product);
    }

    private void assertOwnerOrAdmin(Product product, Long currentUserId, boolean admin) {
        if (!admin && !product.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the product owner or an admin can manage this product");
        }
    }

    private ProductResponseDto toResponse(Product product) {
        User seller = product.getSeller();
        Category category = product.getCategory();
        return new ProductResponseDto(
                product.getId(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getCondition(),
                product.getStatus(),
                seller.getId(),
                seller.getFirstName() + " " + seller.getLastName(),
                category.getId(),
                category.getName(),
                product.getIsActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }

    private String normalizeText(String value) {
        return value.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

