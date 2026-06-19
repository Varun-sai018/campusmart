package com.campusmart.productattribute.service;

import com.campusmart.exception.BadRequestException;
import com.campusmart.exception.ProductAttributeNotFoundException;
import com.campusmart.exception.ProductNotFoundException;
import com.campusmart.product.entity.Product;
import com.campusmart.product.repository.ProductRepository;
import com.campusmart.productattribute.dto.ProductAttributeRequestDto;
import com.campusmart.productattribute.dto.ProductAttributeResponseDto;
import com.campusmart.productattribute.entity.ProductAttribute;
import com.campusmart.productattribute.repository.ProductAttributeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductAttributeService {

    private static final int MAX_ATTRIBUTES_PER_PRODUCT = 20;

    private final ProductAttributeRepository productAttributeRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductAttributeResponseDto createAttribute(
            Long productId,
            ProductAttributeRequestDto request,
            Long currentUserId,
            boolean admin
    ) {
        Product product = getActiveProduct(productId);
        assertOwnerOrAdmin(product, currentUserId, admin);

        if (productAttributeRepository.countByProduct(product) >= MAX_ATTRIBUTES_PER_PRODUCT) {
            throw new BadRequestException("A product can have at most 20 attributes");
        }

        ProductAttribute attribute = ProductAttribute.builder()
                .attributeName(request.attributeName().trim())
                .attributeValue(request.attributeValue().trim())
                .product(product)
                .build();

        return toDto(productAttributeRepository.save(attribute));
    }

    @Transactional(readOnly = true)
    public List<ProductAttributeResponseDto> getAttributesByProduct(Long productId) {
        Product product = getActiveProduct(productId);
        return productAttributeRepository.findByProductOrderByCreatedAtAsc(product)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void deleteAttribute(Long attributeId, Long currentUserId, boolean admin) {
        ProductAttribute attribute = productAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new ProductAttributeNotFoundException(attributeId));

        Product product = attribute.getProduct();
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new ProductNotFoundException(product.getId());
        }
        assertOwnerOrAdmin(product, currentUserId, admin);

        productAttributeRepository.delete(attribute);
    }

    private Product getActiveProduct(Long productId) {
        return productRepository.findById(productId)
                .filter(product -> Boolean.TRUE.equals(product.getIsActive()))
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private void assertOwnerOrAdmin(Product product, Long currentUserId, boolean admin) {
        if (!admin && !product.getSeller().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Only the product owner or an admin can manage product attributes");
        }
    }

    private ProductAttributeResponseDto toDto(ProductAttribute attribute) {
        return new ProductAttributeResponseDto(
                attribute.getId(),
                attribute.getAttributeName(),
                attribute.getAttributeValue(),
                attribute.getProduct().getId(),
                attribute.getCreatedAt()
        );
    }
}
