package com.binarylabyrinth.notificationservice.provider.impl;

import com.binarylabyrinth.notificationservice.provider.PushProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MockPushProvider - Mock implementation of PushProvider
 *
 * This is a placeholder implementation that logs push notifications.
 * In production, replace with actual push notification provider integration (Firebase FCM, APNs, etc.)
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Slf4j
@Component
public class MockPushProvider implements PushProvider {

    @Override
    public boolean send(String deviceToken, String title, String message) throws Exception {
        log.info("Sending Push Notification via Mock Provider to device {}: {} - {}",
                deviceToken, title, message);
        // In production, this would call Firebase Cloud Messaging API or APNs API
        // Example Firebase:
        // Message msg = Message.builder()
        //     .putData("title", title)
        //     .putData("message", message)
        //     .setToken(deviceToken)
        //     .build();
        // FirebaseMessaging.getInstance().send(msg);
        return true;
    }

    @Override
    public String getProviderName() {
        return "Mock Push Provider";
    }
}

