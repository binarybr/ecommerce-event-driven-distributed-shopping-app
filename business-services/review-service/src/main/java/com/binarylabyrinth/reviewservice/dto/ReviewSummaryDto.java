package com.binarylabyrinth.reviewservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated rating summary for a product card / product detail page.
 * Returned by GET /api/reviews/product/{productId}/summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryDto {

    private String productId;

    /** Average rating, 0.0 if no reviews exist (NaN-safe). */
    private Double averageRating;

    /** Number of reviews. */
    private Long totalReviews;
}
