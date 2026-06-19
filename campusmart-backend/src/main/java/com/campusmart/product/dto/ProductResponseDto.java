package com.campusmart.product.dto;

import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

