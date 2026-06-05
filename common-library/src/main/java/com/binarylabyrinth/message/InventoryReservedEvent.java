package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * InventoryReservedEvent - Kafka Event Message
 *
 * This event is published by the Inventory Service when:
 * 1. An order is received (via order-placed event)
 * 2. Stock is verified to be available
 * 3. Stock is successfully reserved for the order
 *
 * This event signals SUCCESS in the inventory check process.
 * Downstream services (like Notification) can use this to confirm stock was reserved.
 *
 * Kafka Topic: "inventory-reserved"
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Order number being processed - links back to original order */
    private String orderNumber;

    /** Product ID that was reserved */
    private String productId;

    /** Quantity that was successfully reserved */
    private Integer quantity;

    /** Timestamp of when inventory was reserved */
    private LocalDateTime reservedAt;
}
