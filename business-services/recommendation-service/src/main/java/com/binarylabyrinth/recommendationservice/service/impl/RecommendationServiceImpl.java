package com.binarylabyrinth.recommendationservice.service.impl;

import com.binarylabyrinth.recommendationservice.dto.RecommendationDto;
import com.binarylabyrinth.recommendationservice.dto.RecommendationResponse;
import com.binarylabyrinth.recommendationservice.repository.UserInteractionRepository;
import com.binarylabyrinth.recommendationservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserInteractionRepository repository;

    @Override
    public RecommendationResponse recommendForProduct(String productId, int limit) {
        Pageable pageable = PageRequest.of(0, sane(limit));
        List<RecommendationDto> items = toDtos(repository.findCoPurchaseRecommendations(productId, pageable));
        log.debug("co-purchase for {} returned {} items", productId, items.size());
        return RecommendationResponse.builder()
                .strategy("co-purchase")
                .basedOn(productId)
                .items(items)
                .build();
    }

    @Override
    public RecommendationResponse recommendForUser(String userEmail, int limit) {
        Pageable pageable = PageRequest.of(0, sane(limit));
        List<RecommendationDto> items = toDtos(repository.findPersonalRecommendations(userEmail, pageable));
        log.debug("personalized for {} returned {} items", userEmail, items.size());
        return RecommendationResponse.builder()
                .strategy("personalized")
                .basedOn(userEmail)
                .items(items)
                .build();
    }

    @Override
    public RecommendationResponse recommendTrending(int limit) {
        Pageable pageable = PageRequest.of(0, sane(limit));
        List<RecommendationDto> items = toDtos(repository.findTrending(pageable));
        return RecommendationResponse.builder()
                .strategy("trending")
                .basedOn(null)
                .items(items)
                .build();
    }

    // ------------- helpers -------------

    private static int sane(int limit) {
        if (limit < 1) return 5;
        return Math.min(limit, 50);
    }

    /**
     * Each row from the native query is Object[2]:
     *   [0] = product_id (String)
     *   [1] = score (BigDecimal in MySQL/Hibernate, fall back to Number cast)
     */
    private static List<RecommendationDto> toDtos(List<Object[]> rows) {
        return rows.stream().map(r -> RecommendationDto.builder()
                .productId((String) r[0])
                .score(((Number) r[1]).doubleValue())
                .build()).toList();
    }
}
