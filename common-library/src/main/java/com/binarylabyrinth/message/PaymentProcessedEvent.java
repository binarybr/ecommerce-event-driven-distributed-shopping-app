package com.binarylabyrinth.message;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentProcessedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long paymentId;
    private String orderId;
    private String userId;
    private Double amount;
    private String currency;
    private String transactionId;
    private LocalDateTime processedAt;
}
