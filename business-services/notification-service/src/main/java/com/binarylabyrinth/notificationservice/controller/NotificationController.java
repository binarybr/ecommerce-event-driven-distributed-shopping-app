package com.binarylabyrinth.notificationservice.controller;

import com.binarylabyrinth.notificationservice.dto.NotificationRequestDto;
import com.binarylabyrinth.notificationservice.entity.Notification;
import com.binarylabyrinth.notificationservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * NotificationController - REST API Endpoints for Notifications
 *
 * Provides REST endpoints for sending different types of notifications:
 * - Email: POST /api/notifications/email
 * - SMS: POST /api/notifications/sms
 * - Push: POST /api/notifications/push
 * - Retrieve: GET /api/notifications (retrieve all notifications)
 * - Retrieve One: GET /api/notifications/{id}
 *
 * The service also listens to Kafka events and sends automated notifications:
 * - When order is placed → Email confirmation
 * - When inventory is reserved → System alerts
 *
 * All POST endpoints return HTTP 202 Accepted (async processing).
 * Notification sending happens asynchronously in the background.
 *
 * Endpoint Base Path: /api/notifications
 * Service: Notification Service (Port 8084)
 * Via API Gateway: http://localhost:8080/api/notifications/**
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    /** NotificationService implementation - injected by Spring */
    private final NotificationService notificationService;

    /**
     * POST /api/notifications/email - Send email notification
     *
     * Accepts email details and sends notification asynchronously.
     * Returns immediately with HTTP 202 (Accepted), actual sending happens in background.
     *
     * @param requestDto Email data (recipient, subject, message - validated)
     * @return Empty ResponseEntity (HTTP 202 Accepted)
     */
    @PostMapping("/email")
    public ResponseEntity<Void> sendEmail(
            @Valid @RequestBody NotificationRequestDto requestDto) {

        notificationService.sendEmail(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * POST /api/notifications/sms - Send SMS notification
     *
     * Accepts SMS details and sends notification asynchronously.
     * Currently logs the request (SMS provider integration for future).
     * Returns immediately with HTTP 202 (Accepted).
     *
     * @param requestDto SMS data (recipient, message - validated)
     * @return Empty ResponseEntity (HTTP 202 Accepted)
     */
    @PostMapping("/sms")
    public ResponseEntity<Void> sendSms(
            @Valid @RequestBody NotificationRequestDto requestDto) {

        notificationService.sendSms(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * POST /api/notifications/push - Send push notification
     *
     * Accepts push notification details and sends asynchronously.
     * Currently logs the request (Push provider integration for future).
     * Returns immediately with HTTP 202 (Accepted).
     *
     * @param requestDto Push notification data (recipient, message - validated)
     * @return Empty ResponseEntity (HTTP 202 Accepted)
     */
    @PostMapping("/push")
    public ResponseEntity<Void> sendPush(
            @Valid @RequestBody NotificationRequestDto requestDto) {

        notificationService.sendPushNotification(requestDto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    /**
     * GET /api/notifications - Retrieve all notifications
     *
     * Returns all notification records from the database.
     * Useful for audit trails and notification history.
     *
     * @return ResponseEntity with list of all notification records (HTTP 200)
     */
    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications() {

        return ResponseEntity.ok(notificationService.getNotifications());
    }

    /**
     * GET /api/notifications/{id} - Retrieve a notification by ID
     *
     * Retrieves specific notification record by its database ID.
     * Includes sending status, timestamp, and error messages if any.
     *
     * @param id Notification ID (database primary key)
     * @return ResponseEntity with notification details (HTTP 200)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotification(
            @PathVariable Long id) {

        return ResponseEntity.ok(notificationService.getNotification(id));
    }
}
