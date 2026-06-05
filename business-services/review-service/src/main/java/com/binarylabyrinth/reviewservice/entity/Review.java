package com.binarylabyrinth.reviewservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Review entity — one row per (user, product) pair.
 *
 * Unique constraint on (user_email, product_id) enforces "one review per user
 * per product" — attempts to insert a duplicate throw
 * DataIntegrityViolationException which the service translates to a 409.
 */
@Entity
@Table(name = "reviews", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_email", "product_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** MongoDB ObjectId of the reviewed product */
    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    /** User email from JWT subject (review owner) */
    @Column(name = "user_email", nullable = false, length = 255)
    private String userEmail;

    /** Numeric user id from JWT (denormalized for analytics joins) */
    @Column(name = "user_id")
    private Long userId;

    /** Star rating, 1-5 (validated at DTO layer) */
    @Column(nullable = false)
    private Integer rating;

    /** Optional free-text comment */
    @Column(length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
