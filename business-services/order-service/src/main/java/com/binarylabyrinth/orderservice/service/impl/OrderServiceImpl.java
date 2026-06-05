package com.binarylabyrinth.orderservice.service.impl;

import com.binarylabyrinth.message.OrderCancelledEvent;
import com.binarylabyrinth.message.OrderPlacedEvent;
import com.binarylabyrinth.orderservice.client.InventoryClient;
import com.binarylabyrinth.orderservice.client.ProductClient;
import com.binarylabyrinth.orderservice.client.UserClient;
import com.binarylabyrinth.orderservice.dto.InventoryResponseDto;
import com.binarylabyrinth.orderservice.dto.OrderRequestDto;
import com.binarylabyrinth.orderservice.dto.OrderResponseDto;
import com.binarylabyrinth.orderservice.dto.ProductDto;
import com.binarylabyrinth.orderservice.dto.UserDto;
import com.binarylabyrinth.orderservice.entity.Order;
import com.binarylabyrinth.orderservice.exception.OrderNotFoundException;
import com.binarylabyrinth.orderservice.exception.ProductOutOfStockException;
import com.binarylabyrinth.orderservice.mapper.OrderMapper;
import com.binarylabyrinth.orderservice.repository.OrderRepository;
import com.binarylabyrinth.orderservice.service.OrderService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * OrderServiceImpl - Business Logic for Order Management
 *
 * Core responsibilities:
 * 1. Validate orders against inventory availability
 * 2. Create orders atomically
 * 3. Publish OrderPlacedEvent to Kafka
 * 4. Implement resilience patterns (circuit breaker)
 * 5. Handle inter-service communication via Feign
 * 6. Retrieve and manage orders
 *
 * KEY PATTERNS:
 * 1. CIRCUIT BREAKER (Resilience4j):
 *    - Protects against cascading failures in Inventory Service
 *    - If Inventory Service is down, circuit breaker opens
 *    - Fallback method provides graceful degradation
 *    - Sliding window: 10 requests
 *    - Failure threshold: 50%
 *    - Wait duration: 10 seconds before retrying
 *
 * 2. FEIGN CLIENT:
 *    - Synchronous RPC call to Inventory Service
 *    - Service discovery via Eureka (lb://inventory-service)
 *    - Checks product stock before creating order
 *
 * 3. EVENT SOURCING:
 *    - OrderPlacedEvent published to Kafka topic "order-placed"
 *    - Triggers downstream services (Inventory, Notification)
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl
        implements OrderService {

    /** MySQL repository for order persistence */
    private final OrderRepository orderRepository;

    /** Mapper for DTO ↔ Entity conversions */
    private final OrderMapper orderMapper;

    /** Feign client for calling Inventory Service */
    private final InventoryClient inventoryClient;

    /** Feign client for calling Product Service (authoritative pricing) */
    private final ProductClient productClient;

    /** Feign client for calling User Service */
    private final UserClient userClient;

    /** Kafka template for publishing events */
    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Place a new order
     *
     * Process flow:
     * 1. Call Inventory Service via Feign client to check stock
     *    - Protected by Circuit Breaker pattern
     *    - Falls back if Inventory Service is unavailable
     * 2. If stock available: Create order, publish event
     * 3. If stock unavailable: Throw ProductOutOfStockException
     * 4. Save order to MySQL database
     * 5. Generate unique order number
     * 6. Set order status to "PLACED"
     * 7. Publish OrderPlacedEvent to Kafka
     * 8. Log operation
     * 9. Return response DTO to client
     *
     * CIRCUIT BREAKER CONFIG:
     * - Name: "inventory"
     * - Fallback method: fallbackMethod()
     * - Sliding window size: 10
     * - Failure rate threshold: 50%
     * - Wait duration before retry: 10 seconds
     *
     * @param requestDto Order request from client (validated)
     * @return OrderResponseDto Order created successfully
     * @throws ProductOutOfStockException if product out of stock
     * @throws RuntimeException if Inventory Service is down (via fallback)
     */
    @Override
    @CircuitBreaker(
            name = "inventory",
            fallbackMethod = "fallbackMethod")
    public OrderResponseDto placeOrder(
            OrderRequestDto requestDto){

        // Call Inventory Service via Feign client with circuit breaker protection.
        // NOTE: despite the name isInStock(), this maps to POST /api/inventory/reserve
        // and ACTUALLY DECREMENTS (reserves) stock as a side effect. Therefore, if
        // any step AFTER this point throws (e.g. orderRepository.save fails), the
        // reserved units would leak — the order won't exist but stock stays reduced.
        // A fully robust design would reserve only after the order is persisted, or
        // compensate via an order-cancelled event on failure (deleteOrder already
        // emits that event on the cancel path).
        InventoryResponseDto inventoryResponse =
                inventoryClient.isInStock(
                        requestDto.getProductId(),
                        requestDto.getQuantity());

        // Check if product is in stock
        if(!inventoryResponse.isInStock()){

            throw new ProductOutOfStockException(
                    "Product out of stock");
        }

        // SECURITY: never trust the client-supplied price. Fetch the current
        // unit price from product-service and recompute the authoritative order
        // total as unitPrice * quantity. This prevents a forged-price under-pay
        // attack. The client's requestDto.getPrice() is intentionally ignored.
        double authoritativePrice = resolveAuthoritativePrice(
                requestDto.getProductId(), requestDto.getQuantity());

        // Convert DTO to Entity
        Order order =
                orderMapper.toEntity(requestDto);

        // Override with the server-computed price (not the client's value)
        order.setPrice(authoritativePrice);

        // Generate unique order number (UUID)
        order.setOrderNumber(
                UUID.randomUUID().toString());

        // Set initial status
        order.setStatus("PLACED");

        // Set creation timestamp
        order.setCreatedAt(LocalDateTime.now());

         // Persist to MySQL database
         Order savedOrder =
                 orderRepository.save(order);

         // Fetch customer email for notification
         String customerEmail = "";
         try {
             UserDto user = userClient.getUserById(requestDto.getUserId());
             customerEmail = user.getEmail();
         } catch (Exception e) {
             log.warn("Could not fetch user email for user: {}, error: {}",
                     requestDto.getUserId(), e.getMessage());
             // Continue with order - notification can use default or user ID
         }

         // Publish OrderPlacedEvent to Kafka
         kafkaTemplate.send(
                 "order-placed",
                 OrderPlacedEvent.builder()
                         .userId(savedOrder.getUserId())
                         .customerEmail(customerEmail)
                         .orderNumber(savedOrder.getOrderNumber())
                         .productId(savedOrder.getProductId())
                         .quantity(savedOrder.getQuantity())
                         .orderAmount(savedOrder.getPrice())
                         .placedAt(LocalDateTime.now())
                         .build());

        // Log for monitoring
        log.info(
                "Order placed successfully : {}",
                savedOrder.getOrderNumber());

        // Convert entity to response DTO
        return orderMapper.toResponseDto(
                savedOrder);
    }

    /**
     * Fallback method for Circuit Breaker
     *
     * Called when:
     * 1. Inventory Service is unavailable
     * 2. Circuit breaker is open
     * 3. Too many failures detected
     *
     * Provides graceful error handling.
     *
     * @param requestDto Original order request
     * @param ex Exception that triggered fallback
     * @return Never returns - always throws exception
     * @throws RuntimeException with user-friendly message
     */
    public OrderResponseDto fallbackMethod(
            OrderRequestDto requestDto,
            Exception ex){

        // Re-throw business exceptions untouched so the controller's
        // GlobalExceptionHandler can map them to their proper HTTP status
        // (e.g. ProductOutOfStockException -> 400). Only genuine infrastructure
        // failures should surface as the generic "unavailable" message.
        if (ex instanceof ProductOutOfStockException) {
            throw (ProductOutOfStockException) ex;
        }
        throw new RuntimeException(
                "Inventory service unavailable. Please try later.");
    }

    /**
     * Resolve the authoritative order total from the product catalog.
     *
     * Fetches the current unit price from product-service via Feign and returns
     * unitPrice * quantity. This is the single source of truth for billing — the
     * client-supplied price is never used. If the product is missing or has no
     * price, the order is rejected rather than billed at an unknown amount.
     */
    private double resolveAuthoritativePrice(String productId, int quantity) {
        try {
            ProductDto product = productClient.getProductById(productId);
            if (product == null || product.getPrice() == null) {
                throw new ProductOutOfStockException(
                        "Cannot price order: product " + productId + " has no catalog price");
            }
            return product.getPrice() * quantity;
        } catch (feign.FeignException.NotFound e) {
            throw new ProductOutOfStockException(
                    "Cannot place order: product " + productId + " not found in catalog");
        }
    }

    /**
     * Retrieve all orders
     *
     * @return List of all orders from MySQL
     */
    @Override
    public List<OrderResponseDto> getAllOrders(){

        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponseDto)
                .toList();
    }

    /**
     * Retrieve an order by ID
     *
     * @param id Order ID (database primary key)
     * @return OrderResponseDto found order
     * @throws OrderNotFoundException if order not found
     */
    @Override
    public OrderResponseDto getOrderById(
            Long id){

        // Query MySQL for order
        Order order =
                orderRepository.findById(id)
                        .orElseThrow(() ->
                                new OrderNotFoundException(id));

        // Convert entity to response DTO
        return orderMapper.toResponseDto(order);
    }

    /**
     * Delete an order
     *
     * Soft deletion pattern could be implemented here
     * by setting status to "CANCELLED" instead of hard delete.
     *
     * @param id Order ID to delete
     * @throws OrderNotFoundException if order not found
     */
    @Override
    public OrderResponseDto updateOrderStatus(Long id, String newStatus){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        order.setStatus(newStatus);
        Order saved = orderRepository.save(order);
        log.info("Order {} status updated to {}", id, newStatus);
        return orderMapper.toResponseDto(saved);
    }

    @Override
    public void deleteOrder(Long id){

        // Query MySQL for order
        Order order =
                orderRepository.findById(id)
                        .orElseThrow(() ->
                                new OrderNotFoundException(id));

        // Delete from MySQL
        orderRepository.delete(order);

        // Publish order-cancelled so inventory-service restocks the reserved
        // units (otherwise the reservation leaks). Only meaningful if the order
        // had been reserved (status not already CANCELLED).
        if (!"CANCELLED".equalsIgnoreCase(order.getStatus())) {
            kafkaTemplate.send("order-cancelled",
                    OrderCancelledEvent.builder()
                            .orderNumber(order.getOrderNumber())
                            .productId(order.getProductId())
                            .quantity(order.getQuantity())
                            .cancelledAt(LocalDateTime.now())
                            .build());
        }

        // Log for monitoring
        log.info(
                "Order deleted successfully : {}",
                id);
    }
}
