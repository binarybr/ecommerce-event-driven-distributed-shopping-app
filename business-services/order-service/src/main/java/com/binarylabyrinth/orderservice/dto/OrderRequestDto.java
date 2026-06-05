package com.binarylabyrinth.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

/**
 * OrderRequestDto - API Request Data Transfer Object
 * 
 * This DTO represents the data structure expected in API POST requests
 * for placing new orders.
 * 
 * Validation Annotations:
 * - @NotBlank: Field cannot be null or empty (for strings)
 * - @NotNull: Field cannot be null (for numeric types)
 * - @Min: Minimum value constraint
 * - @Positive: Must be greater than zero
 * 
 * Used by: OrderController.placeOrder()
 * 
 * Example JSON Request Body:
 * {
 *   "productId": "507f1f77bcf86cd799439011",
 *   "quantity": 2,
 *   "price": 199.98
 * }
 * 
 * @author Binary Labyrinth
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {

    /**
     * User ID who is placing the order - cannot be blank
     * Validation: Required field
     */
    @NotBlank(message = "User ID is required")
    private String userId;

    /**
     * Product ID to order - cannot be blank
     * Format: MongoDB ObjectId as String
     * Validation: Required field
     */
    @NotBlank(message = "Product ID is required")
    private String productId;

    /**
     * Quantity of product to order - cannot be null, minimum 1
     * Validation: Required field, must be >= 1
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    /**
     * Total price for the order - cannot be null, must be positive.
     *
     * SECURITY WARNING (known issue): this price is currently taken from the
     * client request and trusted as-is (see OrderServiceImpl.placeOrder via
     * OrderMapper.toEntity). A malicious client could POST a forged low price
     * and under-pay. The correct fix is to IGNORE this field and recompute the
     * authoritative price server-side: fetch the unit price from product-service
     * (Feign) and multiply by quantity. Until then, treat this value as a hint
     * only and never as the source of truth for billing.
     *
     * Validation: Required field, must be > 0. Format: currency value (e.g. 99.99)
     */
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private Double price;
}