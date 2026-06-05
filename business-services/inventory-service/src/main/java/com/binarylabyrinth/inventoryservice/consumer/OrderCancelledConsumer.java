package com.binarylabyrinth.inventoryservice.consumer;

import com.binarylabyrinth.message.OrderCancelledEvent;
import com.binarylabyrinth.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Restocks inventory when an order is cancelled/deleted, so previously
 * reserved units don't leak. Consumes "order-cancelled" published by
 * order-service.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCancelledConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "order-cancelled", groupId = "inventory-group")
    public void consume(OrderCancelledEvent event) {
        log.info("order-cancelled received for order {}: releasing {} units of product {}",
                event.getOrderNumber(), event.getQuantity(), event.getProductId());

        if (event.getProductId() != null && event.getQuantity() != null) {
            inventoryService.releaseStock(event.getProductId(), event.getQuantity());
        }
    }
}
