package com.binarylabyrinth.reviewservice.repository;

import com.binarylabyrinth.reviewservice.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);

    List<Review> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    Optional<Review> findByUserEmailAndProductId(String userEmail, String productId);

    long countByProductId(String productId);

    /**
     * Average rating for a product. Returns null when there are no reviews —
     * caller must coalesce to 0.0.
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double averageRatingForProduct(@Param("productId") String productId);
}
