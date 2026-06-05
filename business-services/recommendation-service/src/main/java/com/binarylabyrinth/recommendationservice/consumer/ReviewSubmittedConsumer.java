package com.binarylabyrinth.recommendationservice.consumer;

import com.binarylabyrinth.message.ReviewSubmittedEvent;
import com.binarylabyrinth.recommendationservice.entity.InteractionType;
import com.binarylabyrinth.recommendationservice.service.InteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewSubmittedConsumer {

    private final InteractionService interactionService;

    @KafkaListener(topics = "review-submitted", groupId = "recommendation-group")
    public void consume(ReviewSubmittedEvent event) {
        log.info("review-submitted received: user={}, product={}, rating={}",
                event.getUserEmail(), event.getProductId(), event.getRating());

        interactionService.recordInteraction(
                event.getUserEmail(),
                event.getProductId(),
                InteractionType.REVIEW);
    }
}
