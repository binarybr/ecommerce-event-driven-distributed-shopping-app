package com.binarylabyrinth.cartservice.service;

import com.binarylabyrinth.cartservice.dto.AddToCartDto;
import com.binarylabyrinth.cartservice.dto.CartItemResponseDto;
import com.binarylabyrinth.cartservice.dto.CartResponseDto;

public interface CartService {
    CartResponseDto addItem(String userId, AddToCartDto request);

    CartResponseDto updateItem(String userId, Long itemId, Integer quantity);

    void removeItem(String userId, Long itemId);

    CartResponseDto getCart(String userId);

    void clearCart(String userId);
}
