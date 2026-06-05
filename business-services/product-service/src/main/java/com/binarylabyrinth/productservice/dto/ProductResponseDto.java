package com.binarylabyrinth.productservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductResponseDto - API Response Data Transfer Object
 *
 * Returned by all read endpoints. Includes the MongoDB ObjectId plus all
 * enriched catalog fields needed for product cards / search result tiles.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {

    private String id;

    private String name;

    private String description;

    private Double price;

    private String category;

    private String brand;

    private String sku;

    private String imageUrl;

    private List<String> tags;

    private Integer stock;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
