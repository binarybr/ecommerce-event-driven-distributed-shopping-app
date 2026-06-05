package com.binarylabyrinth.notificationservice.service.impl;

import com.binarylabyrinth.notificationservice.dto.NotificationRequestDto;
import com.binarylabyrinth.notificationservice.entity.Notification;
import com.binarylabyrinth.notificationservice.exception.NotificationException;
import com.binarylabyrinth.notificationservice.provider.PushProvider;
import com.binarylabyrinth.notificationservice.provider.SmsProvider;
import com.binarylabyrinth.notificationservice.repository.NotificationRepository;
import com.binarylabyrinth.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NotificationServiceImpl - Business Logic for Notifications
 *
 * Core responsibilities:
 * 1. Send email notifications via SMTP
 * 2. Log SMS notifications (producer integration ready)
 * 3. Log push notifications (provider integration ready)
 * 4. Persist all notifications to MySQL for audit trail
 * 5. Track notification status (PENDING → SENT/FAILED)
 * 6. Handle errors gracefully with persistence
 *
 * NOTIFICATION TYPES:
 * 1. EMAIL:
 *    - Uses JavaMailSender (Spring Mail integration)
 *    - SMTP configuration in application.yaml
 *    - Status: SENT on successful delivery
 *
 * 2. SMS:
 *    - Currently logs to database (Twilio/AWS SNS integration ready)
 *    - Can be enhanced with SMS provider SDK
 *    - Status: SENT (logged to database)
 *
 * 3. PUSH:
 *    - Currently logs to database (Firebase/APNs integration ready)
 *    - Can be enhanced with push notification provider SDK
 *    - Status: SENT (logged to database)
 *
 * DATABASE PERSISTENCE:
 * - All notifications saved to MySQL regardless of outcome
 * - Allows for audit trail and retry logic
 * - Tracks timestamps and error messages
 * - Enables notification resend functionality
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl
        implements NotificationService {

    /** JavaMailSender for sending emails via SMTP */
    private final JavaMailSender mailSender;

    /** Repository for persisting notifications to MySQL */
    private final NotificationRepository notificationRepository;

    /** SMS provider for sending SMS notifications */
    private final SmsProvider smsProvider;

    /** Push provider for sending push notifications */
    private final PushProvider pushProvider;

    /**
     * Send email notification
     *
     * Process flow:
     * 1. Create notification record (PENDING status)
     * 2. Persist to MySQL
     * 3. Attempt to send via SMTP
     * 4. Update status to SENT or FAILED
     * 5. Log the operation
     *
     * SMTP Configuration:
     * - Host: ${MAIL_HOST:localhost}
     * - Port: ${MAIL_PORT:1025}
     * - Auth: Configurable
     * - TLS: Configurable
     *
     * Uses SimpleMailMessage for basic email support.
     * Can be enhanced with MimeMessage for HTML emails.
     *
     * @param requestDto Email details (recipient, subject, message)
     * @throws NotificationException if email sending fails
     */
    @Override
    public void sendEmail(
            NotificationRequestDto requestDto){

        // Step 1: Create notification entity with PENDING status
        Notification notification = Notification.builder()
                .recipient(requestDto.getRecipient())
                .subject(requestDto.getSubject())
                .message(requestDto.getMessage())
                .type("EMAIL")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        // Step 2: Persist to MySQL first (audit trail)
        Notification saved = notificationRepository.save(notification);

        try {
            // Step 3: Create and configure email message
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(requestDto.getRecipient());
            message.setSubject(requestDto.getSubject());
            message.setText(requestDto.getMessage());

            // Step 4: Send email via SMTP
            mailSender.send(message);

            // Step 5: Update notification status to SENT
            saved.setStatus("SENT");
            saved.setSentAt(LocalDateTime.now());
            notificationRepository.save(saved);

            // Step 6: Log successful send
            log.info("Email notification sent successfully: {}", saved.getId());
        }
        // Handle email sending errors
        catch (Exception ex){

            // Update notification status to FAILED with error message
            saved.setStatus("FAILED");
            saved.setErrorMessage(ex.getMessage());
            notificationRepository.save(saved);

            // Log error
            log.error("Failed to send email notification: {}", saved.getId(), ex);

            // Throw exception for caller to handle
            throw new NotificationException("Failed to send email: " + ex.getMessage());
        }
    }

    /**
     * Send SMS notification
     *
     * Current implementation:
     * - Persists notification to database with status tracking
     * - Calls SmsProvider to send message
     * - Updates status to SENT or FAILED based on result
     *
     * Supports SMS provider integration (Twilio, AWS SNS, etc.)
     *
     * @param requestDto SMS details (recipient phone, message)
     */
    @Override
    public void sendSms(
            NotificationRequestDto requestDto){

        // Create notification entity with PENDING status
        Notification notification = Notification.builder()
                .recipient(requestDto.getRecipient())
                .subject(requestDto.getSubject())
                .message(requestDto.getMessage())
                .type("SMS")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        // Persist to MySQL
        Notification saved = notificationRepository.save(notification);

        try {
            // Send SMS via provider
            boolean sent = smsProvider.send(requestDto.getRecipient(), requestDto.getMessage());

            if (sent) {
                // Update status to SENT on success
                saved.setStatus("SENT");
                saved.setSentAt(LocalDateTime.now());
                log.info("SMS notification sent successfully: {} via {}",
                        saved.getId(), smsProvider.getProviderName());
            } else {
                // Update status to FAILED if provider returned false
                saved.setStatus("FAILED");
                saved.setErrorMessage("SMS provider returned false");
                log.warn("SMS notification failed for: {}", saved.getId());
            }

        } catch (Exception ex) {
            // Handle provider errors
            saved.setStatus("FAILED");
            saved.setErrorMessage(ex.getMessage());
            log.error("Failed to send SMS notification: {}", saved.getId(), ex);
        }

        // Persist final status
        notificationRepository.save(saved);
    }

    /**
     * Send push notification
     *
     * Current implementation:
     * - Persists notification to database with status tracking
     * - Calls PushProvider to send message
     * - Updates status to SENT or FAILED based on result
     *
     * Supports push notification provider integration (Firebase FCM, APNs, etc.)
     *
     * @param requestDto Push notification details (device ID, message)
     */
    @Override
    public void sendPushNotification(
            NotificationRequestDto requestDto){

        // Create notification entity with PENDING status
        Notification notification = Notification.builder()
                .recipient(requestDto.getRecipient())
                .subject(requestDto.getSubject())
                .message(requestDto.getMessage())
                .type("PUSH")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        // Persist to MySQL
        Notification saved = notificationRepository.save(notification);

        try {
            // Send push notification via provider
            boolean sent = pushProvider.send(
                    requestDto.getRecipient(),
                    requestDto.getSubject(),
                    requestDto.getMessage());

            if (sent) {
                // Update status to SENT on success
                saved.setStatus("SENT");
                saved.setSentAt(LocalDateTime.now());
                log.info("Push notification sent successfully: {} via {}",
                        saved.getId(), pushProvider.getProviderName());
            } else {
                // Update status to FAILED if provider returned false
                saved.setStatus("FAILED");
                saved.setErrorMessage("Push provider returned false");
                log.warn("Push notification failed for: {}", saved.getId());
            }

        } catch (Exception ex) {
            // Handle provider errors
            saved.setStatus("FAILED");
            saved.setErrorMessage(ex.getMessage());
            log.error("Failed to send push notification: {}", saved.getId(), ex);
        }

        // Persist final status
        notificationRepository.save(saved);
    }

    /**
     * Retrieve all notifications from database
     *
     * Process flow:
     * 1. Query MySQL for all notification records
     * 2. Return complete list
     *
     * Called by: NotificationController.getNotifications() via GET /api/notifications
     *
     * Use cases:
     * 1. Admin dashboard: View all notifications
     * 2. Audit trail: Check notification history
     * 3. Reporting: Generate notification statistics
     * 4. Troubleshooting: Identify failed notifications
     *
     * @return List of all notifications (empty list if none exist)
     */
    @Override
    public List<Notification> getNotifications(){

        // Query all notifications from MySQL
        return notificationRepository.findAll();
    }

    /**
     * Retrieve a single notification by ID
     *
     * Process flow:
     * 1. Query MySQL for notification by ID
     * 2. Return notification if found
     *
     * Called by: NotificationController.getNotification() via GET /api/notifications/{id}
     *
     * Use cases:
     * 1. Check notification status (SENT, FAILED, PENDING)
     * 2. View error message if sending failed
     * 3. Verify timestamps (when created, when sent)
     * 4. Customer support: Track communication history
     *
     * @param id Notification ID (database primary key)
     * @return Notification object with all details
     */
    @Override
    public Notification getNotification(Long id){

        // Query notification by ID from MySQL
        return notificationRepository.findById(id)
                .orElse(null);
    }
}
