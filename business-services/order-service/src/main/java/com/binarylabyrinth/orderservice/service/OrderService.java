package com.binarylabyrinth.orderservice.service;

import com.binarylabyrinth.orderservice.dto.OrderRequestDto;
import com.binarylabyrinth.orderservice.dto.OrderResponseDto;

import java.util.List;

/**
 * OrderService - Business Logic Interface
 *
 * Defines core service operations for order management.
 * This interface is implemented by OrderServiceImpl which handles:
 * - Order creation and validation
 * - Kafka event publishing
 * - Database persistence
 * - Feign client calls to other services
 * - Error handling
 *
 * Implementation: OrderServiceImpl
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface OrderService {

    /**
     * Place a new order
     * - Validates input
     * - Persists to MySQL
     * - Publishes Kafka event (order-placed)
     * - Notifies Inventory Service
     */
    OrderResponseDto placeOrder(
            OrderRequestDto requestDto);

    /**
     * Retrieve all orders
     * - Returns all orders from database
     * - Supports pagination in future
     */
    List<OrderResponseDto> getAllOrders();

    /**
     * Retrieve a single order by ID
     * - Returns order if found
     * - Throws OrderNotFoundException if not found
     */
    OrderResponseDto getOrderById(
            Long id);

    /**
     * Delete/Cancel an order by ID
     * - Removes order from database
     * - Throws OrderNotFoundException if not found
     */
    void deleteOrder(Long id);

    /** Update the status of an order (admin only). */
    OrderResponseDto updateOrderStatus(Long id, String newStatus);
}