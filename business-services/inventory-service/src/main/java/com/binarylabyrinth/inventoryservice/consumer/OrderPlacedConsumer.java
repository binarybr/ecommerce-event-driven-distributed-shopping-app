package com.binarylabyrinth.inventoryservice.consumer;

import com.binarylabyrinth.message.InventoryFailedEvent;
import com.binarylabyrinth.message.InventoryReservedEvent;
import com.binarylabyrinth.message.OrderPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * OrderPlacedConsumer - Kafka Event Consumer
 *
 * Stock is reserved synchronously by Order Service via the InventoryClient
 * Feign call before the order is persisted. This consumer reacts to the
 * resulting "order-placed" event and emits a downstream reservation event
 * for other services (e.g. notifications). It does NOT call the inventory
 * service again — doing so would decrement stock a second time.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @KafkaListener(
            topics = "order-placed",
            groupId = "inventory-group")
    public void consume(OrderPlacedEvent event){

        log.info("Received order placed event : {}", event.getOrderNumber());

        try {
            kafkaTemplate.send("inventory-reserved",
                    InventoryReservedEvent.builder()
                            .orderNumber(event.getOrderNumber())
                            .productId(event.getProductId())
                            .quantity(event.getQuantity())
                            .reservedAt(LocalDateTime.now())
                            .build());

            log.info("Inventory reserved event published for order : {}", event.getOrderNumber());
        }
        catch (Exception ex) {

            log.error("Error publishing inventory reserved event", ex);

            kafkaTemplate.send("inventory-failed",
                    InventoryFailedEvent.builder()
                            .orderNumber(event.getOrderNumber())
                            .productId(event.getProductId())
                            .reason("Error: " + ex.getMessage())
                            .failedAt(LocalDateTime.now())
                            .build());
        }
    }
}
