package com.binarylabyrinth.recommendationservice.service;

import com.binarylabyrinth.recommendationservice.entity.InteractionType;

/**
 * Used by Kafka consumers to record events into the user_interactions table.
 * Hides the duplicate-suppression logic from consumers.
 */
public interface InteractionService {

    /**
     * Record an interaction. If (userEmail, productId, type) already exists,
     * this is a no-op — keeps the table stable under at-least-once delivery.
     */
    void recordInteraction(String userEmail, String productId, InteractionType type);
}
