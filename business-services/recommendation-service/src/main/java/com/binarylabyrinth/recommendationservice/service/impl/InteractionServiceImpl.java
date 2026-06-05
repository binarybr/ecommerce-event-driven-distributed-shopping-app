package com.binarylabyrinth.recommendationservice.service.impl;

import com.binarylabyrinth.recommendationservice.entity.InteractionType;
import com.binarylabyrinth.recommendationservice.entity.UserInteraction;
import com.binarylabyrinth.recommendationservice.repository.UserInteractionRepository;
import com.binarylabyrinth.recommendationservice.service.InteractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class InteractionServiceImpl implements InteractionService {

    private final UserInteractionRepository repository;

    @Override
    public void recordInteraction(String userEmail, String productId, InteractionType type) {
        if (userEmail == null || productId == null || type == null) {
            log.warn("Skipping interaction with null fields: user={}, product={}, type={}", userEmail, productId, type);
            return;
        }

        // Quick check first to avoid hitting the unique-constraint exception
        // path for the common idempotent case.
        if (repository.findByUserEmailAndProductIdAndType(userEmail, productId, type).isPresent()) {
            log.debug("Interaction already recorded: {} / {} / {}", userEmail, productId, type);
            return;
        }

        UserInteraction interaction = UserInteraction.builder()
                .userEmail(userEmail)
                .productId(productId)
                .type(type)
                .weight(type == InteractionType.PURCHASE ? 3.0 : 1.0)
                .build();
        try {
            repository.save(interaction);
            log.info("Recorded {} interaction: user={}, product={}", type, userEmail, productId);
        } catch (DataIntegrityViolationException ex) {
            // Race: another consumer thread inserted the same row between our
            // check and save. Safe to swallow — the row exists either way.
            log.debug("Concurrent insert suppressed: {} / {} / {}", userEmail, productId, type);
        }
    }
}
