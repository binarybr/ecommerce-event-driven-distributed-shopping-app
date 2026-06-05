package com.binarylabyrinth.recommendationservice.service;

import com.binarylabyrinth.recommendationservice.dto.RecommendationResponse;

public interface RecommendationService {

    RecommendationResponse recommendForProduct(String productId, int limit);

    RecommendationResponse recommendForUser(String userEmail, int limit);

    RecommendationResponse recommendTrending(int limit);
}
