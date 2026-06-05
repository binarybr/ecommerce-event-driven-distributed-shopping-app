package com.binarylabyrinth.reviewservice.service;

import com.binarylabyrinth.reviewservice.dto.ReviewRequestDto;
import com.binarylabyrinth.reviewservice.dto.ReviewResponseDto;
import com.binarylabyrinth.reviewservice.dto.ReviewSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {

    ReviewResponseDto createReview(ReviewRequestDto request, String userEmail, Long userId);

    Page<ReviewResponseDto> getReviewsForProduct(String productId, Pageable pageable);

    ReviewSummaryDto getSummaryForProduct(String productId);

    List<ReviewResponseDto> getMyReviews(String userEmail);

    ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto request, String userEmail);

    void deleteReview(Long reviewId, String userEmail, boolean isAdmin);
}
