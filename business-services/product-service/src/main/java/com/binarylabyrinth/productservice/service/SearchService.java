package com.binarylabyrinth.productservice.service;

import com.binarylabyrinth.productservice.dto.ProductResponseDto;
import com.binarylabyrinth.productservice.dto.SearchRequestDto;
import com.binarylabyrinth.productservice.dto.SearchResponseDto;
import com.binarylabyrinth.productservice.mapper.ProductMapper;
import com.binarylabyrinth.productservice.repository.ProductSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * SearchService - thin orchestrator that delegates to ProductSearchRepository
 * for the heavy MongoDB work and assembles the response DTO.
 *
 * Kept separate from ProductServiceImpl so the search code path can evolve
 * independently — e.g., adding query logging, click-tracking, A/B test sort
 * variants, or future migration to Elasticsearch.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductSearchRepository searchRepository;
    private final ProductMapper productMapper;

    public SearchResponseDto search(SearchRequestDto request) {
        log.info("Search: q='{}', category='{}', brand='{}', price=[{}..{}], sortBy='{}'",
                request.getQ(), request.getCategory(), request.getBrand(),
                request.getMinPrice(), request.getMaxPrice(), request.getSortBy());

        ProductSearchRepository.SearchResult result = searchRepository.search(request);

        List<ProductResponseDto> dtos = result.products().stream()
                .map(productMapper::toResponseDto)
                .toList();

        int totalPages = result.size() == 0
                ? 0
                : (int) Math.ceil((double) result.total() / result.size());

        return SearchResponseDto.builder()
                .results(dtos)
                .total(result.total())
                .page(result.page())
                .size(result.size())
                .totalPages(totalPages)
                .appliedQuery(request)
                .build();
    }
}
