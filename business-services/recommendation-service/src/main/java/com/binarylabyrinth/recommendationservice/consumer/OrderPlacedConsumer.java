package com.binarylabyrinth.recommendationservice.consumer;

import com.binarylabyrinth.message.OrderPlacedEvent;
import com.binarylabyrinth.recommendationservice.entity.InteractionType;
import com.binarylabyrinth.recommendationservice.service.InteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Consumes order-placed events emitted by order-service. The user identifier
 * carried on the event is the numeric user id; we use it as a key — review
 * events bring email instead, but both work as long as we're consistent
 * inside this service for joining (we just use the same column).
 *
 * Note: we record the interaction against event.getUserId() converted to a
 * string. This means co-purchase joins work across PURCHASE events alone, but
 * REVIEW events (keyed by email) won't co-join unless the user has both.
 *
 * TODO (future): unify identifiers — either always email or always numeric.
 * For now both work standalone and the personalized recommendation falls
 * back to trending if the user has no joinable history.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private final InteractionService interactionService;

    @KafkaListener(topics = "order-placed", groupId = "recommendation-group")
    public void consume(OrderPlacedEvent event) {
        log.info("order-placed received: user={}, email={}, product={}, orderNumber={}",
                event.getUserId(), event.getCustomerEmail(),
                event.getProductId(), event.getOrderNumber());

        // Prefer customerEmail because reviews are keyed by email — same key
        // means PURCHASE and REVIEW rows for the same user will SELF-JOIN in
        // the co-purchase query. Fall back to numeric userId when email is
        // blank (uses hasText, not just != null, because order-service emits
        // "" not null when the user-service Feign lookup fails).
        String userKey;
        if (StringUtils.hasText(event.getCustomerEmail())) {
            userKey = event.getCustomerEmail();
        } else if (StringUtils.hasText(event.getUserId())) {
            userKey = event.getUserId();
            log.warn("Order {} has no customerEmail; falling back to userId={}",
                    event.getOrderNumber(), event.getUserId());
        } else {
            log.error("Order {} has neither customerEmail nor userId — skipping",
                    event.getOrderNumber());
            return;
        }
        interactionService.recordInteraction(userKey, event.getProductId(), InteractionType.PURCHASE);
    }
}
