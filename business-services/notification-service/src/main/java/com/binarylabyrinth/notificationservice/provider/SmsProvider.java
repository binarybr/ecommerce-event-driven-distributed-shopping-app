package com.binarylabyrinth.notificationservice.provider;

/**
 * SmsProvider - Interface for SMS notification providers
 *
 * This interface defines the contract for SMS notification services.
 * Implementations can use Twilio, AWS SNS, or other SMS providers.
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
public interface SmsProvider {

    /**
     * Send SMS message to a phone number
     *
     * @param phoneNumber Recipient phone number
     * @param message Message content
     * @return true if message sent successfully, false otherwise
     * @throws Exception if sending fails
     */
    boolean send(String phoneNumber, String message) throws Exception;

    /**
     * Get provider name
     *
     * @return Name of the SMS provider
     */
    String getProviderName();
}

