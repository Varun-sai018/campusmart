package com.campusmart.order.dto;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long id,
        Long productId,
        String productTitle,
        Long sellerId,
        BigDecimal priceAtPurchase,
        Integer quantity
) {
}
