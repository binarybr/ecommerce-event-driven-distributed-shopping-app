package com.binarylabyrinth.productservice.service;

import com.binarylabyrinth.productservice.dto.ProductRequestDto;
import com.binarylabyrinth.productservice.dto.ProductResponseDto;

import java.util.List;

/**
 * ProductService - Business Logic Interface
 * 
 * Defines core service operations for product management.
 * This interface is implemented by ProductServiceImpl which handles:
 * - Cache management (via Spring annotations)
 * - Kafka event publishing
 * - Database persistence
 * - Error handling
 * 
 * Implementation: ProductServiceImpl
 * 
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface ProductService {

    /**
     * Create a new product
     * - Validates input
     * - Persists to MongoDB
     * - Publishes Kafka event
     * - Invalidates cache
     */
    ProductResponseDto createProduct(
            ProductRequestDto requestDto);

    /**
     * Retrieve all products
     * - Cached for performance
     * - Cache invalidated on modifications
     */
    List<ProductResponseDto> getAllProducts();

    /**
     * Retrieve a single product by ID
     * - Throws ProductNotFoundException if not found
     */
    ProductResponseDto getProductById(
            String id);

    /**
     * Update an existing product
     * - Validates input
     * - Persists to MongoDB
     * - Invalidates cache
     * - Throws ProductNotFoundException if not found
     */
    ProductResponseDto updateProduct(
            String id,
            ProductRequestDto requestDto);

    /**
     * Delete a product by ID
     * - Removes from MongoDB
     * - Invalidates cache
     * - Throws ProductNotFoundException if not found
     */
    void deleteProduct(String id);
}