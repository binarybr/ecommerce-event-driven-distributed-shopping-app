package com.binarylabyrinth.recommendationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Denormalized user-product interaction record.
 *
 * Built by Kafka consumers from order-placed and review-submitted events.
 * The unique constraint on (user_email, product_id, type) means re-emitted
 * events don't create duplicate rows — they get suppressed at insert time.
 *
 * Indexed columns:
 *   - product_id  (lookup "who interacted with this product")
 *   - user_email  (lookup "what did this user interact with")
 *
 * Weight values:
 *   - PURCHASE = 3.0 (strong intent signal)
 *   - REVIEW   = 1.0 (engagement signal, weaker)
 */
@Entity
@Table(name = "user_interactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_email", "product_id", "type"}),
        indexes = {
                @Index(name = "idx_product", columnList = "product_id"),
                @Index(name = "idx_user", columnList = "user_email")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private InteractionType type;

    @Column(nullable = false)
    private Double weight;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
