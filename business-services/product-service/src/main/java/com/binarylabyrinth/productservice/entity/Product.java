package com.binarylabyrinth.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Product - MongoDB Document Entity
 *
 * Stored in the 'products' collection. Text-searchable via MongoDB's $text
 * operator on fields annotated with @TextIndexed. Higher 'weight' values mean
 * matches in that field score higher in relevance ranking.
 *
 * Index strategy:
 *   - id           (auto, primary)
 *   - name         (text, weight 5 - strongest signal)
 *   - brand        (text, weight 3)
 *   - tags         (text, weight 2)
 *   - description  (text, weight 1)
 *   - category     (simple index for filter equality)
 *   - sku          (simple index, unique-ish lookup)
 *
 * Used by:
 *   - Product Service (CRUD)
 *   - SearchService    (full-text + filter queries)
 *   - Cart Service     (Feign call for price lookup)
 *   - Order Service    (productId reference)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "products")
public class Product {

    @Id
    private String id;

    @TextIndexed(weight = 5)
    private String name;

    @TextIndexed(weight = 1)
    private String description;

    private Double price;

    @Indexed
    private String category;

    @TextIndexed(weight = 3)
    private String brand;

    @Indexed
    private String sku;

    private String imageUrl;

    @TextIndexed(weight = 2)
    private List<String> tags;

    private Integer stock;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
