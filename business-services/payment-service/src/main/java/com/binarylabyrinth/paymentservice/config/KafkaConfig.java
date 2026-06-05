package com.binarylabyrinth.paymentservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentProcessedTopic() {
        return new NewTopic("payment-processed", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return new NewTopic("payment-failed", 3, (short) 1);
    }

    @Bean
    public NewTopic paymentRefundedTopic() {
        return new NewTopic("payment-refunded", 3, (short) 1);
    }
}
