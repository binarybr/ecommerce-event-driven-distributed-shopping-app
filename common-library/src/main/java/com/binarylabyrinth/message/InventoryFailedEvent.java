package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * InventoryFailedEvent - Kafka Event Message
 *
 * This event is published by the Inventory Service when:
 * 1. An order is received (via order-placed event)
 * 2. Stock verification fails (insufficient quantity available)
 * 3. OR an error occurs during inventory processing
 *
 * This event signals FAILURE in the inventory check process.
 * The reason for failure should be checked in the 'reason' field.
 *
 * Common Reasons:
 * - "Insufficient quantity"
 * - "Product not found"
 * - "Error: {exception message}"
 *
 * Kafka Topic: "inventory-failed"
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryFailedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Order number that failed - links back to original order */
    private String orderNumber;

    /** Product ID that was checked */
    private String productId;

    /** Reason why the inventory check failed */
    private String reason;

    /** Timestamp of when the failure occurred */
    private LocalDateTime failedAt;
}
