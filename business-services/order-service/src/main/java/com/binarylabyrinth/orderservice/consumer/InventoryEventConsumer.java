package com.binarylabyrinth.orderservice.consumer;

import com.binarylabyrinth.message.InventoryFailedEvent;
import com.binarylabyrinth.message.InventoryReservedEvent;
import com.binarylabyrinth.orderservice.entity.Order;
import com.binarylabyrinth.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * InventoryEventConsumer — drives the order status machine from inventory events.
 *
 * An order is first persisted as PLACED by OrderServiceImpl. Inventory-service then
 * confirms or rejects the reservation asynchronously over Kafka, and this consumer
 * advances the order accordingly:
 *
 *   PLACED --(inventory-reserved)--> CONFIRMED
 *   PLACED --(inventory-failed)----> CANCELLED
 *
 * This is why a freshly placed order quickly transitions to CONFIRMED: the
 * "inventory-reserved" event arrives and flips the status here. Lookups are by
 * orderNumber (the UUID shared across services); a missing order is logged, not fatal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "inventory-reserved", groupId = "order-group")
    public void consumeInventoryReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for order: {}", event.getOrderNumber());

        orderRepository.findByOrderNumber(event.getOrderNumber()).ifPresentOrElse(
                order -> {
                    order.setStatus("CONFIRMED");
                    orderRepository.save(order);
                    log.info("Order {} status updated to CONFIRMED", event.getOrderNumber());
                },
                () -> log.warn("Order not found for orderNumber: {}", event.getOrderNumber())
        );
    }

    @KafkaListener(topics = "inventory-failed", groupId = "order-group")
    public void consumeInventoryFailed(InventoryFailedEvent event) {
        log.warn("Inventory reservation failed for order: {}, reason: {}",
                event.getOrderNumber(), event.getReason());

        orderRepository.findByOrderNumber(event.getOrderNumber()).ifPresentOrElse(
                order -> {
                    order.setStatus("CANCELLED");
                    orderRepository.save(order);
                    log.info("Order {} status updated to CANCELLED due to inventory failure",
                            event.getOrderNumber());
                },
                () -> log.warn("Order not found for orderNumber: {}", event.getOrderNumber())
        );
    }
}
