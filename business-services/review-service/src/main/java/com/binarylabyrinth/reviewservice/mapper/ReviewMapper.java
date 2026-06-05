package com.binarylabyrinth.reviewservice.mapper;

import com.binarylabyrinth.reviewservice.dto.ReviewRequestDto;
import com.binarylabyrinth.reviewservice.dto.ReviewResponseDto;
import com.binarylabyrinth.reviewservice.entity.Review;
import org.springframework.stereotype.Component;

/**
 * ReviewMapper - Review entity ↔ DTO conversions.
 *
 * The owner identity (userEmail/userId) comes from the authenticated JWT, NOT
 * from the request body — a reviewer can't spoof another user's review.
 */
@Component
public class ReviewMapper {

    /** Build a new Review, stamping owner identity from the JWT (not the body). */
    public Review toEntity(ReviewRequestDto request, String userEmail, Long userId) {
        return Review.builder()
                .productId(request.getProductId())
                .userEmail(userEmail)
                .userId(userId)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
    }

    public ReviewResponseDto toResponseDto(Review review) {
        return ReviewResponseDto.builder()
                .id(review.getId())
                .productId(review.getProductId())
                .userEmail(review.getUserEmail())
                .userId(review.getUserId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
