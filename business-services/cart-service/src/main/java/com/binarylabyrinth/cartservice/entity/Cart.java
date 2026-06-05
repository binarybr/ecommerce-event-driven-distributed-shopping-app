package com.binarylabyrinth.cartservice.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart - one shopping cart per user (MySQL, table 'cart').
 *
 * userId is unique (one cart per user) and holds the JWT subject (email).
 * items is the owning side with cascade + orphanRemoval, so clearing/removing
 * from the list deletes the child cart_item rows. EAGER fetch because cart
 * reads almost always need the lines.
 */
@Entity
@Table(name = "cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** One cart per user — the JWT subject (email). Unique. */
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    /** Cart lines; cascade + orphanRemoval so clear()/remove() delete rows. */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Cart() {
    }

    public Cart(String userId) {
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
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

    public static CartBuilder builder() {
        return new CartBuilder();
    }

    public static class CartBuilder {
        private Long id;
        private String userId;
        private List<CartItem> items = new ArrayList<>();
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public CartBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public CartBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public CartBuilder items(List<CartItem> items) {
            this.items = items;
            return this;
        }

        public CartBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public CartBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Cart build() {
            Cart cart = new Cart();
            cart.id = this.id;
            cart.userId = this.userId;
            cart.items = this.items;
            cart.createdAt = this.createdAt;
            cart.updatedAt = this.updatedAt;
            return cart;
        }
    }
}
