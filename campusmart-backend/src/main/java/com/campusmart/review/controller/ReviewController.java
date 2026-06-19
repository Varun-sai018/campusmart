package com.campusmart.review.controller;

import com.campusmart.review.dto.ProductRatingSummaryDto;
import com.campusmart.review.dto.ReviewRequestDto;
import com.campusmart.review.dto.ReviewResponseDto;
import com.campusmart.review.service.ReviewService;
import com.campusmart.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

@Tag(name = "Reviews", description = "Product review and rating APIs")
@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Create a review for a product", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/api/products/{productId}/reviews")
    public ResponseEntity<ReviewResponseDto> createReview(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReviewRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createReview(productId, principal.getId(), request));
    }

    @Operation(summary = "Update a review", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/api/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ReviewRequestDto request
    ) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, principal.getId(), request));
    }

    @Operation(summary = "Delete a review", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/api/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        reviewService.deleteReview(reviewId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get reviews for a product")
    @GetMapping("/api/products/{productId}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewsByProduct(productId));
    }

    @Operation(summary = "Get product rating summary")
    @GetMapping("/api/products/{productId}/rating-summary")
    public ResponseEntity<ProductRatingSummaryDto> getRatingSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getRatingSummary(productId));
    }
}
