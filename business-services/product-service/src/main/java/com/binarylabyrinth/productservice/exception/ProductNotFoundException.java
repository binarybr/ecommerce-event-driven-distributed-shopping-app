package com.binarylabyrinth.productservice.exception;

/**
 * ProductNotFoundException - Custom Exception
 *
 * Thrown when a requested product is not found in the MongoDB database.
 * This is a RuntimeException, so it doesn't need to be explicitly caught or declared.
 *
 * Caught by: GlobalExceptionHandler.handleProductNotFoundException()
 * HTTP Status: 404 NOT_FOUND
 *
 * Usage:
 * if (!product.isPresent()) {
 *     throw new ProductNotFoundException(productId);
 * }
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Constructor - creates exception with formatted message
     *
     * @param id The product ID that was not found
     */
    public ProductNotFoundException(String id) {

        super("Product not found with id : " + id);
    }
}