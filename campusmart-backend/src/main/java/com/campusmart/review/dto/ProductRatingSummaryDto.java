package com.campusmart.review.dto;

import java.util.Map;

public record ProductRatingSummaryDto(
        Double averageRating,
        Long totalReviews,
        Map<Integer, Long> ratingBreakdown
) {
}
