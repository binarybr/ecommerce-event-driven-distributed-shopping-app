package com.binarylabyrinth.paymentservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Payment - JPA entity (MySQL, table 'payment').
 *
 * One row per payment attempt — both successful (COMPLETED) and failed (FAILED)
 * attempts are persisted, giving a full audit trail. A FAILED row carries the
 * Stripe error in errorMessage; a COMPLETED row carries the Stripe charge id in
 * transactionId.
 */
@Entity
@Table(name = "payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owner of the payment — the JWT subject (email) of the paying customer. */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /** Business order reference this payment settles. */
    @Column(name = "order_id", nullable = false)
    private String orderId;

    /** Charge amount in major currency units (e.g. dollars); sent to Stripe as cents. */
    @Column(nullable = false)
    private Double amount;

    /** ISO currency code, e.g. "USD". */
    @Column(nullable = false)
    private String currency;

    /** COMPLETED | FAILED | REFUNDED. */
    @Column(nullable = false)
    private String status;

    /** How it was paid (e.g. "STRIPE"). */
    @Column(name = "payment_method")
    private String paymentMethod;

    /** Stripe charge id — unique so the same charge can't be recorded twice. */
    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    /** Populated on FAILED payments with the gateway's error message. */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** When the attempt was recorded. */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** When the charge succeeded (null for FAILED payments). */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public Payment() {
    }

    public Payment(String userId, String orderId, Double amount, String currency, String status) {
        this.userId = userId;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public static PaymentBuilder builder() {
        return new PaymentBuilder();
    }

    public static class PaymentBuilder {
        private Long id;
        private String userId;
        private String orderId;
        private Double amount;
        private String currency;
        private String status;
        private String paymentMethod;
        private String transactionId;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public PaymentBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PaymentBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public PaymentBuilder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public PaymentBuilder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public PaymentBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public PaymentBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PaymentBuilder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public PaymentBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public PaymentBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public PaymentBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PaymentBuilder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Payment build() {
            Payment payment = new Payment();
            payment.id = this.id;
            payment.userId = this.userId;
            payment.orderId = this.orderId;
            payment.amount = this.amount;
            payment.currency = this.currency;
            payment.status = this.status;
            payment.paymentMethod = this.paymentMethod;
            payment.transactionId = this.transactionId;
            payment.errorMessage = this.errorMessage;
            payment.createdAt = this.createdAt;
            payment.completedAt = this.completedAt;
            return payment;
        }
    }
}
