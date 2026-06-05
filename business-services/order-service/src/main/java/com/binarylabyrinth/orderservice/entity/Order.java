package com.binarylabyrinth.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Order - JPA Entity (MySQL)
 *
 * Represents a customer order in the online shopping application.
 * This entity is persisted to MySQL in the 'orders' table.
 *
 * Order Lifecycle:
 * 1. PLACED - Order initially created, awaiting inventory confirmation
 * 2. CONFIRMED - Inventory has been reserved
 * 3. SHIPPED - Order has been sent to customer
 * 4. DELIVERED - Order reached customer
 * 5. CANCELLED - Order was cancelled
 *
 * Used by: Order Service
 * Database: MySQL (localhost:3306/order_service)
 * Table: orders
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order {

    /** Auto-generated primary key - unique order identifier in database */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique order number (UUID) - visible to customers */
    private String orderNumber;

    /** User ID who placed the order - links to User Service */
    private String userId;

    /** Product ID being ordered - links to Product Service */
    private String productId;

    /** Quantity of product ordered */
    private Integer quantity;

    /** Total price for this order */
    private Double price;

    /** Order status (PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED) */
    private String status;

    /** Timestamp of when the order was created */
    private LocalDateTime createdAt;
}