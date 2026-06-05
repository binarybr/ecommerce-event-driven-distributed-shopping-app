package com.binarylabyrinth.reviewservice.controller;

import com.binarylabyrinth.reviewservice.dto.ReviewRequestDto;
import com.binarylabyrinth.reviewservice.dto.ReviewResponseDto;
import com.binarylabyrinth.reviewservice.dto.ReviewSummaryDto;
import com.binarylabyrinth.reviewservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    // ---------- WRITE endpoints (CUSTOMER/ADMIN required) ----------

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ReviewResponseDto> createReview(
            @Valid @RequestBody ReviewRequestDto request,
            Authentication auth) {

        String email = auth.getName();
        Long userId = (Long) auth.getCredentials();   // filter stuffed userId here
        ReviewResponseDto saved = reviewService.createReview(request, email, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ReviewResponseDto> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequestDto request,
            Authentication auth) {

        return ResponseEntity.ok(reviewService.updateReview(id, request, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, Authentication auth) {
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        reviewService.deleteReview(id, auth.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    // ---------- READ "me" endpoint (auth required) ----------

    @GetMapping("/user/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<ReviewResponseDto>> getMyReviews(Authentication auth) {
        return ResponseEntity.ok(reviewService.getMyReviews(auth.getName()));
    }

    // ---------- PUBLIC read endpoints ----------

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewResponseDto>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(Math.max(size, 1), 100));
        return ResponseEntity.ok(reviewService.getReviewsForProduct(productId, pageable));
    }

    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ReviewSummaryDto> getProductSummary(@PathVariable String productId) {
        return ResponseEntity.ok(reviewService.getSummaryForProduct(productId));
    }
}
