package com.binarylabyrinth.cartservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * AddToCartDto - body for POST /api/cart/items.
 * The owner isn't in the body — it's taken from the JWT. Adding a productId
 * already in the cart merges quantities (see CartServiceImpl.addItem).
 */
public class AddToCartDto {
    /** MongoDB ObjectId of the product to add. */
    @NotBlank(message = "Product ID is required")
    private String productId;

    /** Units to add; must be > 0 (a non-positive quantity is meaningless). */
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    public AddToCartDto() {
    }

    public AddToCartDto(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
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
}
