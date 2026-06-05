package com.binarylabyrinth.cartservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * CartItem - a single line in a shopping cart (MySQL, table 'cart_item').
 *
 * Child of Cart via @ManyToOne; the parent's @OneToMany uses orphanRemoval, so
 * removing this item from Cart.items deletes the row. Indexed on user_id and
 * product_id for fast lookups.
 */
@Entity
@Table(name = "cart_item", indexes = {
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_product_id", columnList = "product_id")
})
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Owning cart (FK cart_id). The non-owning side is Cart.items. */
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    /** Denormalized owner (JWT email) — mirrors the parent cart's userId. */
    @Column(name = "user_id", nullable = false)
    private String userId;

    /** MongoDB ObjectId of the product. */
    @Column(name = "product_id", nullable = false)
    private String productId;

    /** Units of this product in the cart. */
    @Column(nullable = false)
    private Integer quantity;

    /** Price SNAPSHOT taken when added — stable even if the catalog price changes later. */
    @Column(nullable = false)
    private Double price;

    /** When the line was first added. */
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;

    /** Last time the quantity changed. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public CartItem() {
    }

    public CartItem(String userId, String productId, Integer quantity, Double price) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.addedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public static CartItemBuilder builder() {
        return new CartItemBuilder();
    }

    public static class CartItemBuilder {
        private Long id;
        private Cart cart;
        private String userId;
        private String productId;
        private Integer quantity;
        private Double price;
        private LocalDateTime addedAt;
        private LocalDateTime updatedAt;

        public CartItemBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CartItemBuilder cart(Cart cart) {
            this.cart = cart;
            return this;
        }

        public CartItemBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public CartItemBuilder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public CartItemBuilder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public CartItemBuilder price(Double price) {
            this.price = price;
            return this;
        }

        public CartItemBuilder addedAt(LocalDateTime addedAt) {
            this.addedAt = addedAt;
            return this;
        }

        public CartItemBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public CartItem build() {
            CartItem item = new CartItem();
            item.id = this.id;
            item.cart = this.cart;
            item.userId = this.userId;
            item.productId = this.productId;
            item.quantity = this.quantity;
            item.price = this.price;
            item.addedAt = this.addedAt;
            item.updatedAt = this.updatedAt;
            return item;
        }
    }
}
