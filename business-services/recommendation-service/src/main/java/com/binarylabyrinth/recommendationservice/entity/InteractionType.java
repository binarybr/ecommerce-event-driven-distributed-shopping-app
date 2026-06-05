package com.binarylabyrinth.recommendationservice.entity;

/**
 * Type of user-product interaction. Drives weight in the recommendation
 * scoring algorithm — purchases are stronger signals than reviews.
 */
public enum InteractionType {
    PURCHASE,
    REVIEW
}
