package com.binarylabyrinth.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SearchRequestDto - encapsulates a product search query.
 *
 * Bound from query parameters by SearchController. All fields are optional
 * (mix-and-match) — an empty SearchRequest returns all products in pages.
 *
 * Examples:
 *   /api/products/search?q=wireless
 *   /api/products/search?category=Electronics&minPrice=100&maxPrice=500
 *   /api/products/search?q=headphones&brand=Sony&sortBy=price&sortDir=ASC&page=0&size=20
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequestDto {

    /** Free-text query — matched against name/description/brand/tags. */
    private String q;

    /** Exact category filter. */
    private String category;

    /** Exact brand filter. */
    private String brand;

    /** Single tag filter (matches if tag is in the product's tags list). */
    private String tag;

    /** Inclusive lower bound on price. */
    private Double minPrice;

    /** Inclusive upper bound on price. */
    private Double maxPrice;

    /** Only return products with stock greater than zero. */
    private Boolean inStockOnly;

    /**
     * Sort key. Supported: "price", "name", "createdAt", "relevance" (default
     * when q is present; if q is absent it falls back to createdAt DESC).
     */
    @Builder.Default
    private String sortBy = "relevance";

    /** Sort direction: "ASC" or "DESC" (default DESC). */
    @Builder.Default
    private String sortDir = "DESC";

    /** Zero-based page number. */
    @Builder.Default
    private Integer page = 0;

    /** Page size — capped at 100 by the service. */
    @Builder.Default
    private Integer size = 20;
}
