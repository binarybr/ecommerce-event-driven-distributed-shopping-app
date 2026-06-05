package com.binarylabyrinth.notificationservice.consumer;

import com.binarylabyrinth.message.OrderPlacedEvent;
import com.binarylabyrinth.notificationservice.dto.NotificationRequestDto;
import com.binarylabyrinth.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * OrderPlacedConsumer - Kafka Event Consumer for Notifications
 *
 * This component consumes OrderPlacedEvent messages from Kafka and sends order confirmation
 * notifications to customers.
 *
 * EVENT FLOW:
 * 1. Order Service places an order and publishes OrderPlacedEvent to Kafka
 * 2. This consumer receives the event asynchronously
 * 3. Extracts customer email and order details from the event
 * 4. Creates NotificationRequestDto with order confirmation message
 * 5. Sends email notification via NotificationService
 * 6. Email is asynchronously processed and sent (HTTP 202 Accepted)
 *
 * KAFKA CONFIGURATION:
 * - Topic: "order-placed"
 * - Consumer Group: "notification-group"
 * - Pattern: One consumer per Kafka partition for parallel processing
 * - Automatic offset management: Spring handles commit after successful processing
 *
 * NOTIFICATION DETAILS:
 * - Recipient: customer@gmail.com (hardcoded in this version - can be improved)
 * - Subject: "Order Placed Successfully"
 * - Message: Order number and product ID from the event
 * - Channel: Email (via SMTP through NotificationService)
 *
 * BENEFITS OF EVENT-DRIVEN APPROACH:
 * - Decoupled: Order Service doesn't call Notification Service directly
 * - Asynchronous: Order placement isn't blocked by notification sending
 * - Scalable: Multiple notification consumer instances can process events in parallel
 * - Reliable: Kafka persists events, ensuring no messages are lost
 * - Observable: Each event is logged for debugging and audit trails
 *
 * FUTURE IMPROVEMENTS:
 * - Extract customer email from OrderPlacedEvent (currently hardcoded)
 * - Support multiple notification channels (SMS, push notifications)
 * - Retry policy for failed notifications
 * - Delivery status tracking and analytics
 *
 * @author Binary Labyrinth
 * @version 1.0
 * @see NotificationService
 * @see OrderPlacedEvent
 * @see NotificationRequestDto
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    /** NotificationService implementation - injected by Spring */
    private final NotificationService notificationService;

    /**
     * Consume OrderPlacedEvent from Kafka and send notification
     *
     * This method is triggered automatically by Spring when an OrderPlacedEvent arrives
     * on the "order-placed" topic. The processing is asynchronous and non-blocking.
     *
     * Process Flow:
     * 1. Log the received event for audit trail
     * 2. Extract order details from event
     * 3. Build NotificationRequestDto with customer email and order confirmation message
     * 4. Call NotificationService.sendEmail() to queue notification
     * 5. Method returns immediately (async); email is sent in background
     *
     * ERROR HANDLING:
     * - If email sending fails, NotificationService throws NotificationException
     * - Exception is logged but doesn't stop the consumer
     * - Failed notifications can be tracked and retried
     *
     * KAFKA LISTENER CONFIGURATION:
     * - topics = "order-placed": Listen to this specific Kafka topic
     * - groupId = "notification-group": Consumer group for coordinating multiple instances
     *   If multiple notification services are running, each processes different partitions
     *
     * @param event OrderPlacedEvent received from Kafka
     *              Contains full order details:
     *              - orderNumber: Unique order identifier (UUID string)
     *              - productId: Product being ordered
     *              - quantity: Number of units ordered
     *              - orderAmount: Total order price
     *              - placedAt: Order creation timestamp
     *
     * @throws org.springframework.kafka.support.KafkaException if Kafka connectivity issues
     * @see OrderPlacedEvent
     */
    @KafkaListener(
            topics = "order-placed",
            groupId = "notification-group")
    public void consume(OrderPlacedEvent event){

        log.info("Received order placed event : {}", event.getOrderNumber());

        // Build notification request with order confirmation details
        NotificationRequestDto requestDto =
                NotificationRequestDto.builder()
                        .recipient(event.getCustomerEmail() != null && !event.getCustomerEmail().isEmpty()
                                ? event.getCustomerEmail()
                                : "customer@example.com")
                        .subject("Order Placed Successfully")
                        .message("Your order " + event.getOrderNumber() +
                                " has been placed successfully for product: " +
                                event.getProductId())
                        .build();

        // Send email notification asynchronously
        notificationService.sendEmail(requestDto);
    }
}

