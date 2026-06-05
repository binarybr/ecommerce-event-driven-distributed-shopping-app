package com.binarylabyrinth.inventoryservice.repository;

import com.binarylabyrinth.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * InventoryRepository - MySQL Data Access Object
 *
 * Extends JpaRepository to provide CRUD operations for Inventory entity.
 * JpaRepository automatically provides:
 * - save(Inventory): Create or update
 * - findById(Long): Retrieve by primary key
 * - findAll(): Retrieve all inventory records
 * - delete(Inventory): Delete record
 * - count(): Count total records
 * - exists(Long): Check existence
 *
 * Custom Queries:
 * - findByProductId(String): Find inventory by product ID
 *   Used to look up stock for a specific product
 *
 * Generic types:
 * - Inventory: Entity class
 * - Long: ID type (database primary key)
 *
 * Database: MySQL
 * Table: "inventory"
 * Connection: jdbc:mysql://localhost:3306/inventory_service
 *
 * Usage:
 * - InventoryServiceImpl uses this to query and update stock levels
 * - Supports both manual inventory updates and order-based reservations
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory record by product ID
     * Used to locate stock information for a specific product
     *
     * @param productId Product ID (MongoDB ObjectId as String)
     * @return Optional containing Inventory if found, empty otherwise
     */
    Optional<Inventory> findByProductId(
            String productId);
}