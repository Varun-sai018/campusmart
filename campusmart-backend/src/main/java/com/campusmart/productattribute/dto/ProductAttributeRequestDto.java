package com.campusmart.productattribute.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProductAttributeRequestDto(
        @NotBlank(message = "Attribute name is required")
        @Size(max = 120, message = "Attribute name must be at most 120 characters")
        String attributeName,

        @NotBlank(message = "Attribute value is required")
        @Size(max = 500, message = "Attribute value must be at most 500 characters")
        String attributeValue
) {
}
