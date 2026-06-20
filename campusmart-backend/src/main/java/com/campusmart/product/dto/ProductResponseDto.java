package com.campusmart.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;

public record ProductResponseDto(
        Long id,
        String title,
        String description,
        BigDecimal price,
        ProductCondition condition,
        ProductStatus status,
        Long sellerId,
        String sellerName,
        Long categoryId,
        String categoryName,
        Boolean isActive,
        String primaryImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

