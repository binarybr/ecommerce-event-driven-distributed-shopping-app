package com.binarylabyrinth.paymentservice.service;

import com.binarylabyrinth.paymentservice.dto.PaymentRequestDto;
import com.binarylabyrinth.paymentservice.dto.PaymentResponseDto;
import com.binarylabyrinth.paymentservice.dto.RefundRequestDto;

import java.util.List;

public interface PaymentService {
    PaymentResponseDto processPayment(PaymentRequestDto request, String userId);
    PaymentResponseDto getPaymentDetails(Long paymentId);
    PaymentResponseDto refundPayment(Long paymentId, RefundRequestDto request, String userId);
    List<PaymentResponseDto> getPaymentHistory(String userId);
}
