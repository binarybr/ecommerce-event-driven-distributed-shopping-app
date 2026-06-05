package com.binarylabyrinth.productservice.mapper;

import com.binarylabyrinth.productservice.dto.ProductRequestDto;
import com.binarylabyrinth.productservice.dto.ProductResponseDto;
import com.binarylabyrinth.productservice.entity.Product;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ProductMapper - DTO ↔ Entity conversions.
 *
 * Handles enriched catalog fields (category, brand, tags, stock, etc.).
 * Sets createdAt on new entities so we have a known sort field for "newest".
 */
@Component
public class ProductMapper {

    public Product toEntity(ProductRequestDto requestDto) {
        return Product.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .category(requestDto.getCategory())
                .brand(requestDto.getBrand())
                .sku(requestDto.getSku())
                .imageUrl(requestDto.getImageUrl())
                .tags(requestDto.getTags())
                .stock(requestDto.getStock())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public ProductResponseDto toResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .brand(product.getBrand())
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .tags(product.getTags())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    /**
     * Copy mutable fields from a request onto an existing entity (for updates).
     * Caller is responsible for calling repository.save() after.
     */
    public void updateEntity(Product target, ProductRequestDto src) {
        target.setName(src.getName());
        target.setDescription(src.getDescription());
        target.setPrice(src.getPrice());
        target.setCategory(src.getCategory());
        target.setBrand(src.getBrand());
        target.setSku(src.getSku());
        target.setImageUrl(src.getImageUrl());
        target.setTags(src.getTags());
        target.setStock(src.getStock());
        target.setUpdatedAt(LocalDateTime.now());
    }
}
