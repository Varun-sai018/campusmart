package com.campusmart.category.dto;

import java.time.LocalDateTime;

public record CategoryResponseDto(
        Long id,
        String name,
        String description,
        Boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

