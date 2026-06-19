package com.campusmart.wishlist.dto;

import java.time.LocalDateTime;

public record WishlistItemResponseDto(
        Long id,
        Long userId,
        Long productId,
        String productTitle,
        Boolean productActive,
        LocalDateTime createdAt
) {
}
