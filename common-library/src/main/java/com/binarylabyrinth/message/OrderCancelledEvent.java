package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OrderCancelledEvent - Kafka Event Message
 *
 * Published by order-service when an order is cancelled or deleted. Consumed by
 * inventory-service to RELEASE (restock) the previously reserved quantity, so
 * cancelled orders don't leak inventory.
 *
 * Kafka topic: "order-cancelled"
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private String orderNumber;
    private String productId;
    private Integer quantity;
    private LocalDateTime cancelledAt;
}
