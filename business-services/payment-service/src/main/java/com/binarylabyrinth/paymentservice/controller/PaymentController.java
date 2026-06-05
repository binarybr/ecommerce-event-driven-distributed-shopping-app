package com.binarylabyrinth.paymentservice.controller;

import com.binarylabyrinth.paymentservice.dto.PaymentRequestDto;
import com.binarylabyrinth.paymentservice.dto.PaymentResponseDto;
import com.binarylabyrinth.paymentservice.dto.RefundRequestDto;
import com.binarylabyrinth.paymentservice.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> processPayment(
            @Valid @RequestBody PaymentRequestDto request,
            Authentication authentication) {
        String userId = authentication.getName();
        PaymentResponseDto response = paymentService.processPayment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> getPaymentDetails(
            @PathVariable Long id,
            Authentication authentication) {
        PaymentResponseDto response = paymentService.getPaymentDetails(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<PaymentResponseDto>> getPaymentHistory(
            Authentication authentication) {
        String userId = authentication.getName();
        List<PaymentResponseDto> payments = paymentService.getPaymentHistory(userId);
        return ResponseEntity.ok(payments);
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> refundPayment(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequestDto request,
            Authentication authentication) {
        String userId = authentication.getName();
        PaymentResponseDto response = paymentService.refundPayment(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
