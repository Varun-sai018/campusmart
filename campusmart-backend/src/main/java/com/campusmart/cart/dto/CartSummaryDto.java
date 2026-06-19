package com.campusmart.cart.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartSummaryDto(
        Integer totalItems,
        Integer totalQuantity,
        BigDecimal totalAmount,
        List<CartItemResponseDto> cartItems
) {
}
