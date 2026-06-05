package com.binarylabyrinth.reviewservice.service.impl;

import com.binarylabyrinth.message.ReviewSubmittedEvent;
import com.binarylabyrinth.reviewservice.dto.ReviewRequestDto;
import com.binarylabyrinth.reviewservice.dto.ReviewResponseDto;
import com.binarylabyrinth.reviewservice.dto.ReviewSummaryDto;
import com.binarylabyrinth.reviewservice.entity.Review;
import com.binarylabyrinth.reviewservice.exception.ReviewException;
import com.binarylabyrinth.reviewservice.exception.ReviewNotFoundException;
import com.binarylabyrinth.reviewservice.mapper.ReviewMapper;
import com.binarylabyrinth.reviewservice.repository.ReviewRepository;
import com.binarylabyrinth.reviewservice.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public ReviewResponseDto createReview(ReviewRequestDto request, String userEmail, Long userId) {
        log.info("Creating review for product {} by user {}", request.getProductId(), userEmail);

        try {
            Review review = reviewMapper.toEntity(request, userEmail, userId);
            Review saved = reviewRepository.save(review);
            publishReviewSubmitted(saved);
            return reviewMapper.toResponseDto(saved);
        } catch (DataIntegrityViolationException ex) {
            // Triggered by the (user_email, product_id) unique constraint
            throw new ReviewException("You have already reviewed this product. Use PUT to update.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReviewResponseDto> getReviewsForProduct(String productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable)
                .map(reviewMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewSummaryDto getSummaryForProduct(String productId) {
        long count = reviewRepository.countByProductId(productId);
        double avg = 0.0;
        if (count > 0) {
            Double dbAvg = reviewRepository.averageRatingForProduct(productId);
            if (dbAvg != null) {
                avg = dbAvg;
            }
        }
        return ReviewSummaryDto.builder()
                .productId(productId)
                .averageRating(Math.round(avg * 100.0) / 100.0)  // 2 decimals
                .totalReviews(count)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponseDto> getMyReviews(String userEmail) {
        return reviewRepository.findByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(reviewMapper::toResponseDto)
                .toList();
    }

    @Override
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto request, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));

        if (!review.getUserEmail().equals(userEmail)) {
            throw new ReviewException("You can only update your own reviews.");
        }

        review.setRating(request.getRating());
        review.setComment(request.getComment());
        // productId is not allowed to change — preserve original
        Review updated = reviewRepository.save(review);
        return reviewMapper.toResponseDto(updated);
    }

    @Override
    public void deleteReview(Long reviewId, String userEmail, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found: " + reviewId));

        if (!isAdmin && !review.getUserEmail().equals(userEmail)) {
            throw new ReviewException("You can only delete your own reviews.");
        }
        reviewRepository.delete(review);
    }

    private void publishReviewSubmitted(Review review) {
        try {
            ReviewSubmittedEvent event = ReviewSubmittedEvent.builder()
                    .reviewId(review.getId())
                    .userEmail(review.getUserEmail())
                    .userId(review.getUserId())
                    .productId(review.getProductId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .submittedAt(LocalDateTime.now())
                    .build();
            kafkaTemplate.send("review-submitted", event);
            log.debug("Published review-submitted for reviewId={}", review.getId());
        } catch (Exception ex) {
            // Non-fatal — review is persisted, downstream sync can pick up later.
            log.error("Failed to publish review-submitted event", ex);
        }
    }
}
