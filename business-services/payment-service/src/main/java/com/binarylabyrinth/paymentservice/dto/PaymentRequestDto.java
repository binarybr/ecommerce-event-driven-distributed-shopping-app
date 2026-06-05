package com.binarylabyrinth.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PaymentRequestDto {
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amount;

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Stripe token is required")
    private String stripeToken;

    public PaymentRequestDto() {
    }

    public PaymentRequestDto(Double amount, String orderId, String currency, String stripeToken) {
        this.amount = amount;
        this.orderId = orderId;
        this.currency = currency;
        this.stripeToken = stripeToken;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStripeToken() {
        return stripeToken;
    }

    public void setStripeToken(String stripeToken) {
        this.stripeToken = stripeToken;
    }

    public static PaymentRequestDtoBuilder builder() {
        return new PaymentRequestDtoBuilder();
    }

    public static class PaymentRequestDtoBuilder {
        private Double amount;
        private String orderId;
        private String currency;
        private String stripeToken;

        public PaymentRequestDtoBuilder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public PaymentRequestDtoBuilder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public PaymentRequestDtoBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public PaymentRequestDtoBuilder stripeToken(String stripeToken) {
            this.stripeToken = stripeToken;
            return this;
        }

        public PaymentRequestDto build() {
            PaymentRequestDto dto = new PaymentRequestDto();
            dto.amount = this.amount;
            dto.orderId = this.orderId;
            dto.currency = this.currency;
            dto.stripeToken = this.stripeToken;
            return dto;
        }
    }
}
