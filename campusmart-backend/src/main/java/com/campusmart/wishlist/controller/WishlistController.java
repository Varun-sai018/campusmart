package com.campusmart.wishlist.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusmart.security.UserPrincipal;
import com.campusmart.wishlist.dto.WishlistItemResponseDto;
import com.campusmart.wishlist.service.WishlistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Wishlist", description = "User wishlist management")
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @Operation(summary = "Add a product to the authenticated user wishlist")
    @PostMapping("/{productId}")
    public ResponseEntity<WishlistItemResponseDto> addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(wishlistService.addProductToWishlist(productId, principal.getId()));
    }

    @Operation(summary = "Get wishlist items for the authenticated user")
    @GetMapping
    public ResponseEntity<List<WishlistItemResponseDto>> getWishlist(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(wishlistService.getWishlist(principal.getId()));
    }

    @Operation(summary = "Remove a product from the authenticated user wishlist")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        wishlistService.removeProductFromWishlist(productId, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
