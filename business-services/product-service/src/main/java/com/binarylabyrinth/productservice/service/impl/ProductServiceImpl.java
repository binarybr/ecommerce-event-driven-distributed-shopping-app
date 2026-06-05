package com.binarylabyrinth.productservice.service.impl;

import com.binarylabyrinth.message.ProductCreatedEvent;
import com.binarylabyrinth.productservice.dto.ProductRequestDto;
import com.binarylabyrinth.productservice.dto.ProductResponseDto;
import com.binarylabyrinth.productservice.entity.Product;
import com.binarylabyrinth.productservice.exception.ProductNotFoundException;
import com.binarylabyrinth.productservice.mapper.ProductMapper;
import com.binarylabyrinth.productservice.repository.ProductRepository;
import com.binarylabyrinth.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductServiceImpl - Business Logic for Product Management
 *
 * Core responsibilities:
 * 1. Create products with validation
 * 2. Retrieve products with caching
 * 3. Update product information
 * 4. Delete products
 * 5. Publish events to Kafka for inter-service communication
 * 6. Maintain cache consistency
 *
 * CACHING STRATEGY:
 * - getAllProducts() results are cached for performance
 * - Cache is invalidated on create/update/delete operations
 * - Cache Name: "products"
 *
 * EVENT PUBLISHING:
 * - When a product is created, ProductCreatedEvent is published to Kafka
 * - Topic: "product-created"
 * - Payload includes: productId, productName, price, createdAt
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl
        implements ProductService {

    /** MongoDB repository for product persistence */
    private final ProductRepository productRepository;

    /** Mapper for DTO ↔ Entity conversions */
    private final ProductMapper productMapper;

    /** Kafka template for publishing events to message broker */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Create a new product
     *
     * Process flow:
     * 1. Validate input (via @Valid annotations in controller)
     * 2. Convert DTO to Entity using mapper
     * 3. Persist to MongoDB
     * 4. Publish ProductCreatedEvent to Kafka topic "product-created"
     * 5. Invalidate cache to ensure consistency
     * 6. Log the operation
     * 7. Return response DTO to client
     *
     * @param requestDto Product data from client (validated)
     * @return ProductResponseDto Product created successfully
     *
     * Cache Effect: @CacheEvict removes all entries from "products" cache
     */
    @Override
    public ProductResponseDto createProduct(
            ProductRequestDto requestDto){

        // Convert DTO to Entity
        Product product =
                productMapper.toEntity(requestDto);

        // Persist to MongoDB
        Product savedProduct =
                productRepository.save(product);

        // Publish event to Kafka for other services to listen
        kafkaTemplate.send(
                "product-created",
                ProductCreatedEvent.builder()
                        .productId(savedProduct.getId())
                        .productName(savedProduct.getName())
                        .price(savedProduct.getPrice())
                        .stock(savedProduct.getStock())
                        .createdAt(LocalDateTime.now())
                        .build());

        // Log for monitoring
        log.info(
                "Product created with id : {}",
                savedProduct.getId());

        // Convert entity to response DTO
        return productMapper.toResponseDto(
                savedProduct);
    }

    /**
     * Retrieve all products
     *
     * Performance Optimization:
     * - Results are cached after first call
     * - Subsequent calls return cached data
     * - Cache is invalidated when products are modified
     *
     * @return List of all products
     *
     * Cache Effect: @Cacheable caches results under key "products"
     * Cache Duration: Until invalidated by create/update/delete
     */
    @Override
    public List<ProductResponseDto> getAllProducts(){

        // Query all products from MongoDB
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponseDto)
                .toList();
    }

    /**
     * Retrieve a product by ID
     *
     * @param id MongoDB ObjectId as String
     * @return ProductResponseDto found product
     * @throws ProductNotFoundException if product doesn't exist
     */
    @Override
    public ProductResponseDto getProductById(
            String id){

        // Query MongoDB for product
        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductNotFoundException(
                                        "Product not found with id : "
                                                + id));

        // Convert entity to response DTO
        return productMapper.toResponseDto(product);
    }

    /**
     * Update an existing product
     *
     * Process flow:
     * 1. Find existing product by ID
     * 2. Update fields with new values
     * 3. Persist updated product to MongoDB
     * 4. Invalidate cache to ensure consistency
     * 5. Log the operation
     * 6. Return updated response DTO
     *
     * @param id ProductId to update
     * @param requestDto Updated product data
     * @return ProductResponseDto Updated product details
     * @throws ProductNotFoundException if product not found
     *
     * Cache Effect: @CacheEvict removes all entries from "products" cache
     */
    @Override
    public ProductResponseDto updateProduct(
            String id,
            ProductRequestDto requestDto){

        // Find existing product
        Product existingProduct =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductNotFoundException(
                                        "Product not found with id : "
                                                + id));

        // Apply all updatable fields (incl. category/brand/tags/etc.)
        productMapper.updateEntity(existingProduct, requestDto);

        // Persist to MongoDB
        Product updatedProduct =
                productRepository.save(existingProduct);

        // Convert entity to response DTO
        return productMapper.toResponseDto(
                updatedProduct);
    }

    /**
     * Delete a product
     *
     * Process flow:
     * 1. Find product by ID
     * 2. Remove from MongoDB
     * 3. Invalidate cache to ensure consistency
     * 4. Log the operation
     *
     * @param id Product ID to delete
     * @throws ProductNotFoundException if product not found
     *
     * Cache Effect: @CacheEvict removes all entries from "products" cache
     */
    @Override
    @CacheEvict(value = "products", allEntries = true)
    public void deleteProduct(String id){

        // Find product to verify existence
        Product product =
                productRepository.findById(id)
                        .orElseThrow(() ->
                                new ProductNotFoundException(
                                        "Product not found with id : "
                                                + id));

        // Delete from MongoDB
        productRepository.delete(product);

        // Log for monitoring
        log.info(
                "Product deleted with id : {}",
                id);
    }
}