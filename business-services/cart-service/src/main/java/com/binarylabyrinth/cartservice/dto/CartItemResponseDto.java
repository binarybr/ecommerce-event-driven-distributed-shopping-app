package com.binarylabyrinth.cartservice.dto;

import java.time.LocalDateTime;

public class CartItemResponseDto {
    private Long id;
    private String productId;
    private Integer quantity;
    private Double price;
    private Double subtotal;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;

    public CartItemResponseDto() {
    }

    public CartItemResponseDto(Long id, String productId, Integer quantity, Double price,
                               Double subtotal, LocalDateTime addedAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
