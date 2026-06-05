package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRefundedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long paymentId;
    private String orderId;
    private String userId;
    private Double refundAmount;
    private String reason;
    private String refundId;
    private LocalDateTime refundedAt;
}
