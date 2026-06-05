package com.binarylabyrinth.reviewservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Pre-create Kafka topics on startup so downstream consumers (e.g. the
 * future recommendation-service) can begin subscribing immediately without a
 * race against first-publish.
 */
@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic reviewSubmittedTopic() {
        return TopicBuilder.name("review-submitted")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
