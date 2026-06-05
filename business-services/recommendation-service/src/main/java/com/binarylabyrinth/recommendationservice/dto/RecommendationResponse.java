package com.binarylabyrinth.recommendationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Wraps a recommendation list with a small bit of context so the client knows
 * what the algorithm did (the "basedOn" field is useful for "Because you
 * viewed X" style UI labels).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    /** Algorithm flavor: "co-purchase" | "personalized" | "trending" */
    private String strategy;

    /** Anchor productId for co-purchase, userEmail for personalized, null for trending */
    private String basedOn;

    /** Ordered list of recommendations, highest score first */
    private List<RecommendationDto> items;
}
