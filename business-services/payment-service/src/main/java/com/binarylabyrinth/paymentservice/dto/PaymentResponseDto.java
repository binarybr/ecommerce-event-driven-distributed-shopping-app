package com.binarylabyrinth.paymentservice.dto;

import java.time.LocalDateTime;

public class PaymentResponseDto {
    private Long id;
    private String orderId;
    private String userId;
    private Double amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String transactionId;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public PaymentResponseDto() {
    }

    public PaymentResponseDto(Long id, String orderId, String userId, Double amount, String currency,
                            String status, String paymentMethod, String transactionId,
                            String errorMessage, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public static PaymentResponseDtoBuilder builder() {
        return new PaymentResponseDtoBuilder();
    }

    public static class PaymentResponseDtoBuilder {
        private Long id;
        private String orderId;
        private String userId;
        private Double amount;
        private String currency;
        private String status;
        private String paymentMethod;
        private String transactionId;
        private String errorMessage;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;

        public PaymentResponseDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public PaymentResponseDtoBuilder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public PaymentResponseDtoBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public PaymentResponseDtoBuilder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public PaymentResponseDtoBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public PaymentResponseDtoBuilder status(String status) {
            this.status = status;
            return this;
        }

        public PaymentResponseDtoBuilder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public PaymentResponseDtoBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public PaymentResponseDtoBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public PaymentResponseDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public PaymentResponseDtoBuilder completedAt(LocalDateTime completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public PaymentResponseDto build() {
            PaymentResponseDto dto = new PaymentResponseDto();
            dto.id = this.id;
            dto.orderId = this.orderId;
            dto.userId = this.userId;
            dto.amount = this.amount;
            dto.currency = this.currency;
            dto.status = this.status;
            dto.paymentMethod = this.paymentMethod;
            dto.transactionId = this.transactionId;
            dto.errorMessage = this.errorMessage;
            dto.createdAt = this.createdAt;
            dto.completedAt = this.completedAt;
            return dto;
        }
    }
}
