package com.binarylabyrinth.notificationservice.consumer;

import com.binarylabyrinth.message.PaymentFailedEvent;
import com.binarylabyrinth.message.PaymentProcessedEvent;
import com.binarylabyrinth.notificationservice.dto.NotificationRequestDto;
import com.binarylabyrinth.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "payment-processed", groupId = "notification-group")
    public void consumePaymentProcessed(PaymentProcessedEvent event) {
        log.info("Received payment processed event for payment: {}", event.getPaymentId());

        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .recipient(event.getUserId())
                .subject("Payment Confirmed - Order #" + event.getOrderId())
                .message("Your payment of " + event.getCurrency() + " " + event.getAmount()
                        + " for order #" + event.getOrderId() + " has been processed successfully.\n"
                        + "Transaction ID: " + event.getTransactionId())
                .build();

        try {
            notificationService.sendEmail(requestDto);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation for payment: {}", event.getPaymentId(), e);
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "notification-group")
    public void consumePaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment failed event for payment: {}", event.getPaymentId());

        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .recipient(event.getUserId())
                .subject("Payment Failed - Order #" + event.getOrderId())
                .message("Your payment of " + event.getAmount()
                        + " for order #" + event.getOrderId() + " has failed.\n"
                        + "Reason: " + event.getErrorMessage() + "\n"
                        + "Please try again or contact support.")
                .build();

        try {
            notificationService.sendEmail(requestDto);
        } catch (Exception e) {
            log.error("Failed to send payment failure notification for payment: {}", event.getPaymentId(), e);
        }
    }
}
