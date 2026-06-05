package com.binarylabyrinth.recommendationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single recommended product with its co-purchase score.
 *
 * The score is a relative measure within the response — higher = more
 * relevant. It's not normalized so frontends typically use it for ordering
 * (or compute "percentage of top" for display).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {

    /** MongoDB ObjectId of the recommended product */
    private String productId;

    /** Weighted co-purchase score (higher = more relevant) */
    private Double score;
}
