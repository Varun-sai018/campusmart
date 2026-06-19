package com.campusmart.product.controller;

import com.campusmart.product.dto.ProductCreateRequestDto;
import com.campusmart.product.dto.ProductResponseDto;
import com.campusmart.product.dto.ProductUpdateRequestDto;
import com.campusmart.product.service.ProductService;
import com.campusmart.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Products", description = "Product management APIs")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private static final SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_ADMIN");

    private final ProductService productService;

    @Operation(summary = "Create product", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<ProductResponseDto> createProduct(
            @Valid @RequestBody ProductCreateRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request, principal.getId()));
    }

    @Operation(summary = "Get product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Get all products")
    @GetMapping
    public ResponseEntity<Page<ProductResponseDto>> getAllProducts(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    @Operation(summary = "Get products by category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, pageable));
    }

    @Operation(summary = "Get products by seller")
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<ProductResponseDto>> getProductsBySeller(
            @PathVariable Long sellerId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.getProductsBySeller(sellerId, pageable));
    }

    @Operation(summary = "Update product", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, request, principal.getId(), isAdmin(principal)));
    }

    @Operation(summary = "Delete product", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        productService.deleteProduct(id, principal.getId(), isAdmin(principal));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().contains(ADMIN_AUTHORITY);
    }
}

