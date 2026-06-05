package com.binarylabyrinth.recommendationservice.repository;

import com.binarylabyrinth.recommendationservice.entity.UserInteraction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {

    Optional<UserInteraction> findByUserEmailAndProductIdAndType(
            String userEmail, String productId, com.binarylabyrinth.recommendationservice.entity.InteractionType type);

    List<UserInteraction> findByUserEmail(String userEmail);

    /**
     * Co-purchase: for each user who interacted with :productId, find the OTHER
     * products that same user also interacted with, then aggregate by product
     * weighted by interaction strength.
     *
     * Returns rows of [productId (String), score (Double)] ordered by score DESC.
     */
    @Query(value = """
            SELECT b.product_id, SUM(b.weight) AS score
            FROM user_interactions a
            JOIN user_interactions b
              ON a.user_email = b.user_email
             AND b.product_id <> a.product_id
            WHERE a.product_id = :productId
            GROUP BY b.product_id
            ORDER BY score DESC
            """, nativeQuery = true)
    List<Object[]> findCoPurchaseRecommendations(@Param("productId") String productId, Pageable pageable);

    /**
     * Personalized: products that the given user has NOT interacted with,
     * recommended via the co-purchase relationship over each product the user
     * HAS interacted with.
     */
    @Query(value = """
            SELECT b.product_id, SUM(b.weight) AS score
            FROM user_interactions mine
            JOIN user_interactions other
              ON other.product_id = mine.product_id
             AND other.user_email <> mine.user_email
            JOIN user_interactions b
              ON b.user_email = other.user_email
             AND b.product_id <> mine.product_id
            WHERE mine.user_email = :userEmail
              AND b.product_id NOT IN (
                  SELECT m2.product_id FROM user_interactions m2 WHERE m2.user_email = :userEmail
              )
            GROUP BY b.product_id
            ORDER BY score DESC
            """, nativeQuery = true)
    List<Object[]> findPersonalRecommendations(@Param("userEmail") String userEmail, Pageable pageable);

    /**
     * Trending: products with the highest total interaction weight overall.
     */
    @Query(value = """
            SELECT product_id, SUM(weight) AS score
            FROM user_interactions
            GROUP BY product_id
            ORDER BY score DESC
            """, nativeQuery = true)
    List<Object[]> findTrending(Pageable pageable);
}
