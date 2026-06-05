package com.binarylabyrinth.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SearchResponseDto - paginated search results plus metadata.
 *
 * Returned by GET /api/products/search. Frontends use 'total' and 'totalPages'
 * to render pagination controls; 'appliedQuery' echoes back the resolved
 * parameters so the UI can preserve filter state across navigations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {

    /** The products on this page. */
    private List<ProductResponseDto> results;

    /** Total number of matches across all pages. */
    private long total;

    /** Current zero-based page number. */
    private int page;

    /** Page size used for this request. */
    private int size;

    /** Total page count. */
    private int totalPages;

    /** Echoed back so the UI can show "showing results for ..." */
    private SearchRequestDto appliedQuery;
}
