package com.binarylabyrinth.paymentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * RefundRequestDto - body for POST /api/payments/{id}/refund.
 * Only COMPLETED payments can be refunded (enforced in the service). NOTE: the
 * amount is not yet validated against the original charge — a known follow-up.
 */
public class RefundRequestDto {
    /** Amount to refund (major units). Must be positive. */
    @NotNull(message = "Refund amount is required")
    @Positive(message = "Refund amount must be positive")
    private Double amount;

    /** Audit reason forwarded to Stripe (e.g. "requested_by_customer"). */
    @NotBlank(message = "Reason is required")
    private String reason;

    public RefundRequestDto() {
    }

    public RefundRequestDto(Double amount, String reason) {
        this.amount = amount;
        this.reason = reason;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public static RefundRequestDtoBuilder builder() {
        return new RefundRequestDtoBuilder();
    }

    public static class RefundRequestDtoBuilder {
        private Double amount;
        private String reason;

        public RefundRequestDtoBuilder amount(Double amount) {
            this.amount = amount;
            return this;
        }

        public RefundRequestDtoBuilder reason(String reason) {
            this.reason = reason;
            return this;
        }

        public RefundRequestDto build() {
            RefundRequestDto dto = new RefundRequestDto();
            dto.amount = this.amount;
            dto.reason = this.reason;
            return dto;
        }
    }
}
