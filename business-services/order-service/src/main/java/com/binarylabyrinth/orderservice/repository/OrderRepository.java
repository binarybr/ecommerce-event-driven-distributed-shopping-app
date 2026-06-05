package com.binarylabyrinth.orderservice.repository;

import com.binarylabyrinth.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * OrderRepository - MySQL Data Access Object
 *
 * Extends JpaRepository to provide CRUD operations for Order entity.
 * JpaRepository automatically provides:
 * - save(Order): Create or update
 * - findById(Long): Retrieve by primary key
 * - findAll(): Retrieve all orders
 * - delete(Order): Delete order
 * - count(): Count total orders
 * - exists(Long): Check existence
 *
 * Generic types:
 * - Order: Entity class
 * - Long: ID type (database primary key)
 *
 * Database: MySQL
 * Table: "orders"
 * Connection: jdbc:mysql://localhost:3306/order_service
 *
 * Usage:
 * - OrderServiceImpl uses this to persist and retrieve orders
 * - Transactional queries are automatically handled
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface OrderRepository
        extends JpaRepository<Order, Long> {

    java.util.Optional<Order> findByOrderNumber(String orderNumber);
}