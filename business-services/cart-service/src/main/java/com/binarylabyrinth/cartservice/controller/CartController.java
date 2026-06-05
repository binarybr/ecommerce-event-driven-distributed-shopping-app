package com.binarylabyrinth.cartservice.controller;

import com.binarylabyrinth.cartservice.dto.AddToCartDto;
import com.binarylabyrinth.cartservice.dto.CartResponseDto;
import com.binarylabyrinth.cartservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<CartResponseDto> addItem(
            @Valid @RequestBody AddToCartDto request,
            Authentication authentication) {
        String userId = authentication.getName();
        CartResponseDto response = cartService.addItem(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<CartResponseDto> updateItem(
            @PathVariable Long itemId,
            @RequestParam Integer quantity,
            Authentication authentication) {
        String userId = authentication.getName();
        CartResponseDto response = cartService.updateItem(userId, itemId, quantity);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        String userId = authentication.getName();
        cartService.removeItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<CartResponseDto> getCart(
            Authentication authentication) {
        String userId = authentication.getName();
        CartResponseDto response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Void> clearCart(
            Authentication authentication) {
        String userId = authentication.getName();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
