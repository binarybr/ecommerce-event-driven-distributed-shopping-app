package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ProductCreatedEvent - Kafka Event Message
 *
 * This event is published by the Product Service whenever:
 * 1. A new product is successfully created
 * 2. All product data is persisted to MongoDB
 *
 * This event can trigger downstream processes:
 * - Search indexing services
 * - Notification services (e.g., notify admins)
 * - Analytics services
 *
 * Kafka Topic: "product-created"
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique product identifier (MongoDB ObjectId as String) */
    private String productId;

    /** Product name */
    private String productName;

    /** Product price in currency units */
    private Double price;

    /** Initial catalog stock — used by inventory-service to seed its row. */
    private Integer stock;

    /** Timestamp of when the product was created */
    private LocalDateTime createdAt;
}
