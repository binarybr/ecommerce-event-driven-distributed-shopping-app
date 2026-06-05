package com.binarylabyrinth.productservice.controller;

import com.binarylabyrinth.productservice.dto.ProductRequestDto;
import com.binarylabyrinth.productservice.dto.ProductResponseDto;
import com.binarylabyrinth.productservice.dto.SearchRequestDto;
import com.binarylabyrinth.productservice.dto.SearchResponseDto;
import com.binarylabyrinth.productservice.service.ProductService;
import com.binarylabyrinth.productservice.service.SearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ProductController - REST API Endpoints for Products
 *
 * Provides REST endpoints for product management operations:
 * - Create: POST /api/products
 * - Retrieve All: GET /api/products (with caching)
 * - Retrieve One: GET /api/products/{id}
 * - Update: PUT /api/products/{id}
 * - Delete: DELETE /api/products/{id}
 *
 * All endpoints enforce input validation using Jakarta Validation annotations.
 * Endpoint Base Path: /api/products
 *
 * Service: Product Service (Port 8081)
 * Via API Gateway: http://localhost:8080/api/products/**
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    /** ProductService implementation - injected by Spring */
    private final ProductService productService;

    /** SearchService — full-text + filter queries (MongoDB text indexes) */
    private final SearchService searchService;

    /**
     * POST /api/products - Create a new product
     *
     * Accepts a new product via JSON request body.
     * Triggers:
     * - Validation of input fields
     * - Persistence to MongoDB
     * - Kafka event publishing (product-created)
     * - Cache invalidation
     *
     * @param requestDto Product data from request body (validated)
     * @return ResponseEntity with created product details (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<ProductResponseDto>
    createProduct(
            @Valid
            @RequestBody
            ProductRequestDto requestDto){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        productService.createProduct(
                                requestDto));
    }

    /**
     * GET /api/products - Retrieve all products
     *
     * Returns all products from the database.
     * CACHED: Results are cached for 5 minutes to improve performance.
     *
     * @return ResponseEntity with list of all products (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>>
    getAllProducts(){

        return ResponseEntity.ok(
                productService.getAllProducts());
    }

    /**
     * GET /api/products/search - Full-text + filter search.
     *
     * Query parameters (all optional):
     *   q          — free-text query (matches name/description/brand/tags)
     *   category   — exact category filter
     *   brand      — exact brand filter
     *   tag        — single tag in the product's tags list
     *   minPrice   — inclusive lower bound
     *   maxPrice   — inclusive upper bound
     *   inStockOnly — true to filter out zero-stock products
     *   sortBy     — relevance | price | name | createdAt   (default: relevance)
     *   sortDir    — ASC | DESC                              (default: DESC)
     *   page       — zero-based page number                  (default: 0)
     *   size       — page size (1..100)                      (default: 20)
     *
     * Examples:
     *   /api/products/search?q=wireless
     *   /api/products/search?category=Electronics&minPrice=100&maxPrice=500
     *   /api/products/search?q=headphones&sortBy=price&sortDir=ASC
     *
     * NOTE: This endpoint MUST be declared BEFORE the GET /{id} mapping —
     * Spring matches literal path segments first, but listing it earlier makes
     * the routing intent obvious to future maintainers.
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponseDto> search(@ModelAttribute SearchRequestDto request) {
        return ResponseEntity.ok(searchService.search(request));
    }

    /**
     * GET /api/products/{id} - Retrieve a product by ID
     *
     * Retrieves a specific product by its MongoDB ObjectId.
     * Throws ProductNotFoundException if product doesn't exist.
     *
     * @param id Product ID (MongoDB ObjectId as String)
     * @return ResponseEntity with product details (HTTP 200)
     * @throws ProductNotFoundException if product not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto>
    getProductById(
            @PathVariable String id){

        return ResponseEntity.ok(
                productService.getProductById(id));
    }

    /**
     * PUT /api/products/{id} - Update an existing product
     *
     * Updates product details with new information.
     * Triggers:
     * - Validation of update fields
     * - Persistence to MongoDB
     * - Cache invalidation
     *
     * @param id Product ID to update
     * @param requestDto Updated product data
     * @return ResponseEntity with updated product details (HTTP 200)
     * @throws ProductNotFoundException if product not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto>
    updateProduct(
            @PathVariable String id,
            @Valid
            @RequestBody
            ProductRequestDto requestDto){

        return ResponseEntity.ok(
                productService.updateProduct(
                        id,
                        requestDto));
    }

    /**
     * DELETE /api/products/{id} - Delete a product
     *
     * Removes a product from the database.
     * Triggers:
     * - Removal from MongoDB
     * - Cache invalidation
     *
     * @param id Product ID to delete
     * @return Empty ResponseEntity (HTTP 204)
     * @throws ProductNotFoundException if product not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteProduct(
            @PathVariable String id){

        productService.deleteProduct(id);

        return ResponseEntity.noContent().build();
    }
}