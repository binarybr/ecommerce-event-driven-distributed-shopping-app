package com.binarylabyrinth.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafkaRetryTopic;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * KafkaRetryConfig - enables Spring Kafka's non-blocking retry-topic mechanism.
 *
 * {@code @EnableKafkaRetryTopic} lets @KafkaListener methods annotated with
 * {@code @RetryableTopic} automatically forward failed messages to dedicated
 * retry topics (e.g. *-retry-0, -retry-1) with backoff, and finally to a
 * dead-letter topic (*-dlt) after retries are exhausted — instead of blocking
 * the consumer thread and redelivering forever. This is the mechanism that
 * prevents a single bad email from hammering the broker (the DLQ concern).
 *
 * The scheduler bean below provides the threads that drive the delayed
 * re-delivery of retry-topic messages.
 */
@Configuration
@EnableKafkaRetryTopic
public class KafkaRetryConfig {

    /** Thread pool that schedules delayed re-delivery from retry topics. */
    @Bean
    public ThreadPoolTaskScheduler kafkaRetryTopicScheduler() {

        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("kafka-retry-");
        return scheduler;
    }
}
