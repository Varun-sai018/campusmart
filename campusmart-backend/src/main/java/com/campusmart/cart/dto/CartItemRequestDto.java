package com.campusmart.cart.dto;

import jakarta.validation.constraints.Min;

public record CartItemRequestDto(
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity
) {

    public int quantityOrDefault() {
        return quantity == null ? 1 : quantity;
    }
}
