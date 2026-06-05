package com.binarylabyrinth.cartservice.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartResponseDto {
    private Long id;
    private String userId;
    private List<CartItemResponseDto> items = new ArrayList<>();
    private Double totalAmount;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CartResponseDto() {
    }

    public CartResponseDto(Long id, String userId, List<CartItemResponseDto> items,
                           Double totalAmount, Integer itemCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.itemCount = itemCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public List<CartItemResponseDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponseDto> items) {
        this.items = items;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
