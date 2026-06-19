package com.campusmart.productimage.controller;

import com.campusmart.productimage.dto.ProductImageDto;
import com.campusmart.productimage.service.ProductImageService;
import com.campusmart.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Product Images", description = "Product image upload and management APIs")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImageController {

    private static final SimpleGrantedAuthority ADMIN_AUTHORITY = new SimpleGrantedAuthority("ROLE_ADMIN");

    private final ProductImageService productImageService;

    @Operation(summary = "Upload product image", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImageDto> uploadImage(
            @PathVariable Long productId,
            @Parameter(
                    description = "Image file (jpeg, png, webp; max 5 MB)",
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary")
                    )
            )
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productImageService.uploadImage(productId, file, principal.getId(), isAdmin(principal)));
    }

    @Operation(summary = "List product images")
    @GetMapping("/{productId}/images")
    public ResponseEntity<List<ProductImageDto>> getImages(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.getImagesByProduct(productId));
    }

    @Operation(summary = "Delete product image", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        productImageService.deleteImage(imageId, principal.getId(), isAdmin(principal));
        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().contains(ADMIN_AUTHORITY);
    }
}
