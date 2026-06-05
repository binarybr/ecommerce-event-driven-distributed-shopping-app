package com.binarylabyrinth.notificationservice.consumer;

import com.binarylabyrinth.message.UserRegisteredEvent;
import com.binarylabyrinth.notificationservice.dto.NotificationRequestDto;
import com.binarylabyrinth.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user-registered", groupId = "notification-group")
    public void consume(UserRegisteredEvent event) {
        log.info("Received user registered event for user: {}", event.getUserId());

        NotificationRequestDto requestDto = NotificationRequestDto.builder()
                .recipient(event.getEmail())
                .subject("Welcome to Online Shopping App!")
                .message("Dear " + event.getFirstName() + " " + event.getLastName() + ",\n\n"
                        + "Thank you for registering! Please verify your email to activate your account.\n\n"
                        + "Welcome aboard!")
                .build();

        try {
            notificationService.sendEmail(requestDto);
        } catch (Exception e) {
            log.error("Failed to send welcome email to user: {}", event.getUserId(), e);
        }
    }
}
