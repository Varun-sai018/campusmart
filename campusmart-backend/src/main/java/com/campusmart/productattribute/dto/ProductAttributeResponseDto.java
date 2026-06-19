package com.campusmart.productattribute.dto;

import java.time.LocalDateTime;

public record ProductAttributeResponseDto(
        Long id,
        String attributeName,
        String attributeValue,
        Long productId,
        LocalDateTime createdAt
) {
}
