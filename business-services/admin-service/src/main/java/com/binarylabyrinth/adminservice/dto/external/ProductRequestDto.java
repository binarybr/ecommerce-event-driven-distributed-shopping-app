package com.binarylabyrinth.adminservice.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Mirrors product-service's ProductRequestDto for the bulk-import endpoint.
 * Kept separate from ProductDto (the response shape) so admin can submit
 * products without id/timestamps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequestDto {
    private String name;
    private String description;
    private Double price;
    private String category;
    private String brand;
    private String sku;
    private String imageUrl;
    private List<String> tags;
    private Integer stock;
}
