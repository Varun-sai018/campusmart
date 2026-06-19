package com.campusmart.productattribute.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.campusmart.productattribute.dto.ProductAttributeRequestDto;
import com.campusmart.productattribute.dto.ProductAttributeResponseDto;
import com.campusmart.productattribute.service.ProductAttributeService;
import com.campusmart.security.UserPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product Attributes", description = "Product attribute management APIs")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductAttributeController {

    private static final SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_ADMIN");

    private final ProductAttributeService productAttributeService;

    @Operation(summary = "Create product attribute", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{productId}/attributes")
    public ResponseEntity<ProductAttributeResponseDto> createAttribute(
            @PathVariable Long productId,
            @Valid @RequestBody ProductAttributeRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productAttributeService.createAttribute(productId, request, principal.getId(), isAdmin(principal)));
    }

    @Operation(summary = "Get product attributes")
    @GetMapping("/{productId}/attributes")
    public ResponseEntity<List<ProductAttributeResponseDto>> getAttributes(@PathVariable Long productId) {
        return ResponseEntity.ok(productAttributeService.getAttributesByProduct(productId));
    }

    @Operation(summary = "Delete product attribute", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/attributes/{attributeId}")
    public ResponseEntity<Void> deleteAttribute(
            @PathVariable Long attributeId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        productAttributeService.deleteAttribute(attributeId, principal.getId(), isAdmin(principal));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().contains(ADMIN_AUTHORITY);
    }
}
