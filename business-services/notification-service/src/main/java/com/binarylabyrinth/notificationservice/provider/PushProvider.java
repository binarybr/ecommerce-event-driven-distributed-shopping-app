package com.binarylabyrinth.notificationservice.provider;

/**
 * PushProvider - Interface for Push notification providers
 *
 * This interface defines the contract for push notification services.
 * Implementations can use Firebase Cloud Messaging (FCM), Apple Push Notification service (APNs),
 * or other push notification providers.
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface PushProvider {

    /**
     * Send push notification to a device
     *
     * @param deviceToken Device token / registration ID
     * @param title Notification title
     * @param message Message content
     * @return true if message sent successfully, false otherwise
     * @throws Exception if sending fails
     */
    boolean send(String deviceToken, String title, String message) throws Exception;

    /**
     * Get provider name
     *
     * @return Name of the push notification provider
     */
    String getProviderName();
}

