package com.campusmart.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemResponseDto(
        Long id,
        Long userId,
        Long productId,
        String productTitle,
        BigDecimal productPrice,
        Integer quantity,
        BigDecimal totalPrice,
        Boolean productActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
