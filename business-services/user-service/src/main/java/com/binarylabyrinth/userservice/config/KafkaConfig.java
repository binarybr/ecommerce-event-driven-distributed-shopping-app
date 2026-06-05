package com.binarylabyrinth.userservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userRegisteredTopic() {
        return new NewTopic("user-registered", 3, (short) 1);
    }
}
