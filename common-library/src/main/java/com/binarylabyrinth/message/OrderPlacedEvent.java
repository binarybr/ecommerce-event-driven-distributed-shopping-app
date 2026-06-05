package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * OrderPlacedEvent - Kafka Event Message
 *
 * This event is published whenever an order is successfully placed in the Order Service.
 * It serves as the primary trigger for downstream processes including:
 * - Inventory Service: To reserve/check stock
 * - Notification Service: To send customer notifications
 *
 * ALL SERVICES use this unified event class from common-library to ensure
 * consistent serialization/deserialization across the microservices architecture.
 *
 * Kafka Topics: "order-placed"
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** User ID who placed the order */
    private String userId;

    /** Customer email address for notifications */
    private String customerEmail;

    /** Unique order identifier (UUID-based) */
    private String orderNumber;

    /** Product ID being ordered - used for inventory lookup */
    private String productId;

    /** Quantity ordered */
    private Integer quantity;

    /** Total order amount in currency units */
    private Double orderAmount;

    /** Timestamp of when the order was placed */
    private LocalDateTime placedAt;
}
