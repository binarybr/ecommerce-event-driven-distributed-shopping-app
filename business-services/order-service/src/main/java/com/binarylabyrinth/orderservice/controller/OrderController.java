package com.binarylabyrinth.orderservice.controller;

import com.binarylabyrinth.orderservice.dto.OrderRequestDto;
import com.binarylabyrinth.orderservice.dto.OrderResponseDto;
import com.binarylabyrinth.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * OrderController - REST API Endpoints for Orders
 *
 * Provides REST endpoints for order management operations:
 * - Create: POST /api/orders (place new order)
 * - Retrieve All: GET /api/orders
 * - Retrieve One: GET /api/orders/{id}
 * - Delete: DELETE /api/orders/{id} (cancel order)
 *
 * Order Placement Flow:
 * 1. Client sends POST request with product details
 * 2. Controller validates input (@Valid)
 * 3. OrderService creates order and publishes event
 * 4. Other services listen for order event (Inventory, Notification)
 * 5. Response with OrderResponseDto returned to client
 *
 * All endpoints enforce input validation using Jakarta Validation annotations.
 * Endpoint Base Path: /api/orders
 *
 * Service: Order Service (Port 8083)
 * Via API Gateway: http://localhost:8080/api/orders/**
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    /** OrderService implementation - injected by Spring */
    private final OrderService orderService;

    /**
     * POST /api/orders - Place a new order
     *
     * Accepts order details and creates a new order in the system.
     * Triggers:
     * - Validation of input fields
     * - Order persistence to MySQL
     * - Kafka event publishing (order-placed)
     * - Inventory Service notification
     * - Notification Service alert
     *
     * @param requestDto Order data from request body (validated)
     * @return ResponseEntity with created order details (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<OrderResponseDto>
    placeOrder(
            @Valid
            @RequestBody
            OrderRequestDto requestDto){

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        orderService.placeOrder(
                                requestDto));
    }

    /**
     * GET /api/orders - Retrieve all orders
     *
     * Returns all orders from the database.
     * Supports pagination in future versions.
     *
     * @return ResponseEntity with list of all orders (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>>
    getAllOrders(){

        return ResponseEntity.ok(
                orderService.getAllOrders());
    }

    /**
     * GET /api/orders/{id} - Retrieve an order by ID
     *
     * Retrieves a specific order by its database ID.
     * Throws OrderNotFoundException if order doesn't exist.
     *
     * @param id Order ID (database primary key)
     * @return ResponseEntity with order details (HTTP 200)
     * @throws OrderNotFoundException if order not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto>
    getOrderById(
            @PathVariable Long id){

        return ResponseEntity.ok(
                orderService.getOrderById(id));
    }

    /**
     * DELETE /api/orders/{id} - Delete/Cancel an order
     *
     * Removes an order from the system.
     *
     * @param id Order ID to delete
     * @return Empty ResponseEntity (HTTP 204)
     * @throws OrderNotFoundException if order not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
    deleteOrder(
            @PathVariable Long id){

        orderService.deleteOrder(id);

        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/orders/{id}/status?status=SHIPPED — admin order management.
     *
     * NOTE: order-service has no Spring Security on its classpath, so role
     * enforcement happens upstream in admin-service (@PreAuthorize ROLE_ADMIN)
     * which is the only caller. Direct calls to this endpoint are gated only
     * by the gateway. If order-service later adds spring-security, re-add a
     * method-level role check here.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}