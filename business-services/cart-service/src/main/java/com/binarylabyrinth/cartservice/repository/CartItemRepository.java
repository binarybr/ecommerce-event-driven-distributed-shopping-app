package com.binarylabyrinth.cartservice.repository;

import com.binarylabyrinth.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(String userId);

    Optional<CartItem> findByUserIdAndProductId(String userId, String productId);
}
