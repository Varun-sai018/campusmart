package com.campusmart.cart.controller;

import com.campusmart.cart.dto.CartItemRequestDto;
import com.campusmart.cart.dto.CartItemResponseDto;
import com.campusmart.cart.dto.CartSummaryDto;
import com.campusmart.cart.service.CartService;
import com.campusmart.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "Manage user cart items")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Add a product to the authenticated user's cart")
    @PostMapping("/{productId}")
    public ResponseEntity<CartItemResponseDto> addToCart(
            @PathVariable Long productId,
            @Valid @RequestBody CartItemRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(cartService.addProductToCart(productId, request.quantityOrDefault(), principal.getId()));
    }

    @Operation(summary = "Update quantity for a cart item")
    @PutMapping("/{productId}")
    public ResponseEntity<CartItemResponseDto> updateCartItem(
            @PathVariable Long productId,
            @Valid @RequestBody CartItemRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(cartService.updateCartItemQuantity(productId, request.quantityOrDefault(), principal.getId()));
    }

    @Operation(summary = "Get the authenticated user's cart summary")
    @GetMapping
    public ResponseEntity<CartSummaryDto> getCart(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(cartService.getCart(principal.getId()));
    }

    @Operation(summary = "Remove a product from the authenticated user's cart")
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        cartService.removeProductFromCart(productId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove all items from the authenticated user's cart")
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        cartService.clearCart(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
