package com.binarylabyrinth.recommendationservice.controller;

import com.binarylabyrinth.recommendationservice.dto.RecommendationResponse;
import com.binarylabyrinth.recommendationservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    /** Public: "Customers who bought / reviewed this also bought ..." */
    @GetMapping("/product/{productId}")
    public ResponseEntity<RecommendationResponse> forProduct(
            @PathVariable String productId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(recommendationService.recommendForProduct(productId, limit));
    }

    /** Public: most-interacted products overall (no auth, used for homepage). */
    @GetMapping("/trending")
    public ResponseEntity<RecommendationResponse> trending(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.recommendTrending(limit));
    }

    /** Authenticated: personalized recs based on the caller's history. */
    @GetMapping("/user/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<RecommendationResponse> forMe(
            Authentication auth,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(recommendationService.recommendForUser(auth.getName(), limit));
    }
}
