package com.binarylabyrinth.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Inventory - JPA Entity (MySQL)
 *
 * Represents product inventory (stock levels) in the online shopping application.
 * This entity is persisted to MySQL in the 'inventory' table.
 *
 * Tracks:
 * - Which product is in stock
 * - How many units are available
 * - Updates based on order placement
 *
 * Inventory operations:
 * 1. Add inventory: When new stock arrives
 * 2. Check stock: When order is being placed
 * 3. Reserve stock: When order is confirmed
 * 4. Release stock: When order is cancelled
 *
 * Used by: Inventory Service
 * Database: MySQL (localhost:3306/inventory_service)
 * Table: inventory
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Entity
@Table(name = "inventory")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Inventory {

    /** Auto-generated primary key - unique record identifier */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Product ID - links this inventory to a product (from Product Service) */
    private String productId;

    /** Available quantity of the product in stock */
    private Integer quantity;

    /**
     * Optimistic locking version field.
     * Ensures concurrent updates to the same inventory record are detected
     * and can be retried by callers. JPA will maintain this column.
     */
    @Version
    private Integer version;
}