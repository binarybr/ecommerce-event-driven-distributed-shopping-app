package com.binarylabyrinth.paymentservice.service.impl;

import com.binarylabyrinth.message.PaymentProcessedEvent;
import com.binarylabyrinth.message.PaymentFailedEvent;
import com.binarylabyrinth.message.PaymentRefundedEvent;
import com.binarylabyrinth.paymentservice.dto.PaymentRequestDto;
import com.binarylabyrinth.paymentservice.dto.PaymentResponseDto;
import com.binarylabyrinth.paymentservice.dto.RefundRequestDto;
import com.binarylabyrinth.paymentservice.entity.Payment;
import com.binarylabyrinth.paymentservice.exception.PaymentException;
import com.binarylabyrinth.paymentservice.repository.PaymentRepository;
import com.binarylabyrinth.paymentservice.service.PaymentService;
import com.stripe.model.Charge;
import com.stripe.model.Refund;
import com.stripe.net.RequestOptions;
import com.stripe.exception.StripeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PaymentServiceImpl - Stripe-backed payment processing.
 *
 * Flow: build a Stripe Charge → on success persist a COMPLETED payment and
 * publish "payment-processed"; on StripeException persist a FAILED payment,
 * publish "payment-failed", and rethrow so the caller sees the error.
 *
 * Idempotency: each charge uses an idempotency_key of userId+orderId, so a
 * retried request for the same order won't double-charge the customer.
 *
 * Money is sent to Stripe in integer minor units (amount * 100). Refunds are
 * only permitted against COMPLETED payments.
 */
@Service
// noRollbackFor: when Stripe fails we save a FAILED payment record then rethrow
// PaymentException. Without this, the rethrow would roll back that audit row.
@Transactional(noRollbackFor = PaymentException.class)
public class PaymentServiceImpl implements PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentServiceImpl(PaymentRepository paymentRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public PaymentResponseDto processPayment(PaymentRequestDto request, String userId) {
        log.info("Processing payment for user: {}, order: {}, amount: {}", userId, request.getOrderId(), request.getAmount());

        try {
            // Create Stripe charge
            Map<String, Object> params = new HashMap<>();
            params.put("amount", (int)(request.getAmount() * 100)); // Convert to cents
            params.put("currency", request.getCurrency().toLowerCase());
            params.put("source", request.getStripeToken());
            params.put("description", "Order: " + request.getOrderId());

            // Idempotency must be sent as the Idempotency-Key HEADER (via
            // RequestOptions), NOT as a body parameter — Stripe rejects the
            // latter. Same key (userId-orderId) still prevents double-charges.
            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey(userId + "-" + request.getOrderId())
                    .build();

            Charge charge = Charge.create(params, options);

            // Save payment record
            Payment payment = Payment.builder()
                    .userId(userId)
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status("COMPLETED")
                    .paymentMethod("STRIPE")
                    .transactionId(charge.getId())
                    .completedAt(LocalDateTime.now())
                    .build();

            Payment saved = paymentRepository.save(payment);
            log.info("Payment processed successfully: {}", saved.getId());

            // Publish success event
            publishPaymentProcessedEvent(saved);

            return mapToResponse(saved);

        } catch (StripeException e) {
            log.error("Stripe payment error: {}", e.getMessage());

            // Save failed payment
            Payment failedPayment = Payment.builder()
                    .userId(userId)
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status("FAILED")
                    .paymentMethod("STRIPE")
                    .errorMessage(e.getMessage())
                    .build();

            Payment saved = paymentRepository.save(failedPayment);

            // Publish failure event
            publishPaymentFailedEvent(saved, e.getMessage());

            throw new PaymentException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDto getPaymentDetails(Long paymentId) {
        log.debug("Fetching payment details for ID: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));

        return mapToResponse(payment);
    }

    @Override
    public PaymentResponseDto refundPayment(Long paymentId, RefundRequestDto request, String userId) {
        log.info("Processing refund for payment: {}", paymentId);

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException("Payment not found with ID: " + paymentId));

        if (!"COMPLETED".equals(payment.getStatus())) {
            throw new PaymentException("Can only refund completed payments");
        }

        try {
            // Create Stripe refund
            Map<String, Object> params = new HashMap<>();
            params.put("charge", payment.getTransactionId());
            params.put("amount", (int)(request.getAmount() * 100)); // Convert to cents
            params.put("reason", request.getReason());

            Refund refund = Refund.create(params);

            // Update payment status
            payment.setStatus("REFUNDED");
            Payment updated = paymentRepository.save(payment);

            log.info("Refund processed successfully: {}", refund.getId());

            // Publish refund event
            publishPaymentRefundedEvent(updated, request.getAmount(), refund.getId());

            return mapToResponse(updated);

        } catch (StripeException e) {
            log.error("Stripe refund error: {}", e.getMessage());
            throw new PaymentException("Refund processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDto> getPaymentHistory(String userId) {
        log.debug("Fetching payment history for user: {}", userId);

        List<Payment> payments = paymentRepository.findByUserId(userId);
        return payments.stream().map(this::mapToResponse).toList();
    }

    private PaymentResponseDto mapToResponse(Payment payment) {
        return PaymentResponseDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .errorMessage(payment.getErrorMessage())
                .createdAt(payment.getCreatedAt())
                .completedAt(payment.getCompletedAt())
                .build();
    }

    private void publishPaymentProcessedEvent(Payment payment) {
        try {
            PaymentProcessedEvent event = PaymentProcessedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .currency(payment.getCurrency())
                    .transactionId(payment.getTransactionId())
                    .processedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("payment-processed", event);
            log.info("Payment processed event published for payment: {}", payment.getId());
        } catch (Exception ex) {
            log.error("Error publishing payment processed event", ex);
        }
    }

    private void publishPaymentFailedEvent(Payment payment, String errorMessage) {
        try {
            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .amount(payment.getAmount())
                    .errorMessage(errorMessage)
                    .failedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("payment-failed", event);
            log.info("Payment failed event published for payment: {}", payment.getId());
        } catch (Exception ex) {
            log.error("Error publishing payment failed event", ex);
        }
    }

    private void publishPaymentRefundedEvent(Payment payment, Double refundAmount, String refundId) {
        try {
            PaymentRefundedEvent event = PaymentRefundedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .userId(payment.getUserId())
                    .refundAmount(refundAmount)
                    .refundId(refundId)
                    .refundedAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("payment-refunded", event);
            log.info("Payment refunded event published for payment: {}", payment.getId());
        } catch (Exception ex) {
            log.error("Error publishing payment refunded event", ex);
        }
    }
}
