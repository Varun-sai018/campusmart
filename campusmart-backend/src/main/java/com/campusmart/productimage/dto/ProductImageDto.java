package com.campusmart.productimage.dto;

import java.time.LocalDateTime;

public record ProductImageDto(
        Long id,
        String imageUrl,
        Boolean primaryImage,
        Long productId,
        LocalDateTime createdAt
) {
}
