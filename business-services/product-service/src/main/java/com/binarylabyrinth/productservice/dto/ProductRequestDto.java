package com.binarylabyrinth.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.util.List;

/**
 * ProductRequestDto - API Request DTO for create/update.
 *
 * Mandatory fields: name, description, price.
 * Optional fields: category, brand, sku, imageUrl, tags, stock — these enrich
 * the document and feed the search service's relevance scoring.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Price is required")
    private Double price;

    private String category;

    private String brand;

    private String sku;

    private String imageUrl;

    private List<String> tags;

    @PositiveOrZero(message = "Stock cannot be negative")
    private Integer stock;
}
