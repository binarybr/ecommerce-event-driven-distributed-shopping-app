package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ReviewSubmittedEvent - Kafka Event Message
 *
 * Published when a customer submits a new product review. Consumed by:
 *   - recommendation-service: signal that this user has interacted with the product
 *   - notification-service:   (future) email the seller about new review
 *   - analytics:              (future) aggregate review velocity metrics
 *
 * Kafka topic: "review-submitted"
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSubmittedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Database primary key of the review */
    private Long reviewId;

    /** Email of the user who submitted (from JWT subject) */
    private String userEmail;

    /** Numeric user id from JWT claim */
    private Long userId;

    /** MongoDB ObjectId of the product being reviewed */
    private String productId;

    /** Star rating 1-5 */
    private Integer rating;

    /** Optional comment body */
    private String comment;

    /** Server timestamp when the review was persisted */
    private LocalDateTime submittedAt;
}
