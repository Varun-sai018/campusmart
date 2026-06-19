package com.campusmart.product.dto;

import com.campusmart.product.entity.ProductCondition;
import com.campusmart.product.entity.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductUpdateRequestDto(
        @NotBlank(message = "Product title is required")
        @Size(max = 150, message = "Product title must be at most 150 characters")
        String title,

        @Size(max = 2000, message = "Description must be at most 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Product condition is required")
        ProductCondition condition,

        @NotNull(message = "Product status is required")
        ProductStatus status,

        @NotNull(message = "Category is required")
        Long categoryId
) {
}

