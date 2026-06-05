package com.binarylabyrinth.productservice.repository;

import com.binarylabyrinth.productservice.dto.SearchRequestDto;
import com.binarylabyrinth.productservice.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * ProductSearchRepository - custom MongoDB queries for product search.
 *
 * Composes multiple criteria (text + category + price range + stock) into a
 * single Mongo query using MongoTemplate, then runs a count + a paged find.
 *
 * Relevance ranking:
 *   When a text query is present we use TextQuery (Spring Data's text-search
 *   subclass of Query) which auto-injects the $meta:textScore field used for
 *   sorting by relevance. Plain Query won't honor "score" as a sort field.
 */
@Repository
@RequiredArgsConstructor
public class ProductSearchRepository {

    private final MongoTemplate mongoTemplate;

    public SearchResult search(SearchRequestDto req) {
        boolean hasText = StringUtils.hasText(req.getQ());
        String sortBy = req.getSortBy() == null ? "relevance" : req.getSortBy();
        boolean sortByRelevance = "relevance".equalsIgnoreCase(sortBy);

        // --- build query (TextQuery if free-text, regular Query otherwise) ---
        Query query;
        if (hasText) {
            TextCriteria text = TextCriteria.forDefaultLanguage().matchingAny(req.getQ());
            TextQuery tq = TextQuery.queryText(text);
            if (sortByRelevance) {
                tq.sortByScore();
            }
            query = tq;
        } else {
            query = new Query();
        }

        // --- filter criteria ---
        if (StringUtils.hasText(req.getCategory())) {
            query.addCriteria(Criteria.where("category").is(req.getCategory()));
        }
        if (StringUtils.hasText(req.getBrand())) {
            query.addCriteria(Criteria.where("brand").is(req.getBrand()));
        }
        if (StringUtils.hasText(req.getTag())) {
            query.addCriteria(Criteria.where("tags").in(req.getTag()));
        }
        if (req.getMinPrice() != null || req.getMaxPrice() != null) {
            Criteria price = Criteria.where("price");
            if (req.getMinPrice() != null) price.gte(req.getMinPrice());
            if (req.getMaxPrice() != null) price.lte(req.getMaxPrice());
            query.addCriteria(price);
        }
        if (Boolean.TRUE.equals(req.getInStockOnly())) {
            query.addCriteria(Criteria.where("stock").gt(0));
        }

        // --- total count BEFORE applying pagination/sort ---
        long total = mongoTemplate.count(query, Product.class);

        // --- explicit sort (skips when sortByScore() already applied by TextQuery) ---
        applyExplicitSort(query, sortBy, req.getSortDir(), hasText, sortByRelevance);

        // --- pagination ---
        int page = req.getPage() == null || req.getPage() < 0 ? 0 : req.getPage();
        int size = req.getSize() == null ? 20 : Math.min(Math.max(req.getSize(), 1), 100);
        query.skip((long) page * size).limit(size);

        List<Product> products = mongoTemplate.find(query, Product.class);
        return new SearchResult(products, total, page, size);
    }

    private void applyExplicitSort(Query query, String sortBy, String sortDir,
                                   boolean hasText, boolean sortByRelevance) {
        Sort.Direction dir = "ASC".equalsIgnoreCase(sortDir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        switch (sortBy.toLowerCase()) {
            case "price"     -> query.with(Sort.by(dir, "price"));
            case "name"      -> query.with(Sort.by(dir, "name"));
            case "createdat" -> query.with(Sort.by(dir, "createdAt"));
            default -> {
                // "relevance" with text → already set by TextQuery.sortByScore()
                // "relevance" without text → fall back to newest
                if (!hasText) {
                    query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
                }
            }
        }
    }

    /** Value object — controller maps to SearchResponseDto. */
    public record SearchResult(List<Product> products, long total, int page, int size) {}
}
