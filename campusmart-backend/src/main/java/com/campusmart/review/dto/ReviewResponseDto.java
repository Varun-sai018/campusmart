package com.campusmart.review.dto;

import java.time.LocalDateTime;

public record ReviewResponseDto(
        Long id,
        Long buyerId,
        String buyerName,
        Long productId,
        String productTitle,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
