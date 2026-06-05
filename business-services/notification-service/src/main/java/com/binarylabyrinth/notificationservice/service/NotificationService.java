package com.binarylabyrinth.notificationservice.service;

import com.binarylabyrinth.notificationservice.dto.NotificationRequestDto;
import com.binarylabyrinth.notificationservice.entity.Notification;

import java.util.List;

/**
 * NotificationService - Business Logic Interface for Notifications
 *
 * Defines core service operations for notification delivery.
 * This interface is implemented by NotificationServiceImpl which handles:
 * - Sending email notifications via SMTP
 * - Logging SMS notifications (provider integration ready)
 * - Logging push notifications (provider integration ready)
 * - Persisting notification records for audit trails
 * - Tracking notification delivery status
 *
 * Implementation: NotificationServiceImpl
 *
 * Key Responsibilities:
 * 1. Send email notifications (primary channel)
 * 2. Log SMS notification requests
 * 3. Log push notification requests
 * 4. Retrieve notification history and status
 *
 * All send operations return immediately (async processing).
 * Actual notification sending happens in background threads.
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface NotificationService {

    /**
     * Send email notification
     * - Validates recipient and message
     * - Creates notification record in database
     * - Sends email asynchronously
     * - Updates status (PENDING → SENT/FAILED)
     */
    void sendEmail(
            NotificationRequestDto requestDto);

    /**
     * Send SMS notification
     * - Validates recipient and message
     * - Creates notification record in database
     * - Currently logs only (SMS provider integration ready)
     * - Status: PENDING (future: SENT when provider configured)
     */
    void sendSms(
            NotificationRequestDto requestDto);

    /**
     * Send push notification
     * - Validates recipient and message
     * - Creates notification record in database
     * - Currently logs only (Push provider integration ready)
     * - Status: PENDING (future: SENT when provider configured)
     */
    void sendPushNotification(
            NotificationRequestDto requestDto);

    /**
     * Retrieve all notifications
     * - Returns all notification records
     * - Includes sent/failed notifications
     * - Useful for audit trails
     */
    List<Notification> getNotifications();

    /**
     * Retrieve a specific notification by ID
     * - Returns notification with all details
     * - Includes status and error messages if failed
     */
    Notification getNotification(Long id);
}
