package com.binarylabyrinth.notificationservice.provider.impl;

import com.binarylabyrinth.notificationservice.provider.SmsProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MockSmsProvider - Mock implementation of SmsProvider
 *
 * This is a placeholder implementation that logs SMS sends.
 * In production, replace with actual SMS provider integration (Twilio, AWS SNS, etc.)
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@Slf4j
@Component
public class MockSmsProvider implements SmsProvider {

    @Override
    public boolean send(String phoneNumber, String message) throws Exception {
        log.info("Sending SMS via Mock Provider to {}: {}", phoneNumber, message);
        // In production, this would call Twilio API or AWS SNS API
        // Example Twilio:
        // Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        // Message msg = Message.creator(TWILIO_NUMBER, phoneNumber, message).create();
        return true;
    }

    @Override
    public String getProviderName() {
        return "Mock SMS Provider";
    }
}

