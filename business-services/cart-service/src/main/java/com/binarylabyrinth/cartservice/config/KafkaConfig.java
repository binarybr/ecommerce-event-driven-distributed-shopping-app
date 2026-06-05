package com.binarylabyrinth.cartservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic itemAddedToCartTopic() {
        return new NewTopic("item-added-to-cart", 3, (short) 1);
    }

    @Bean
    public NewTopic cartClearedTopic() {
        return new NewTopic("cart-cleared", 3, (short) 1);
    }
}
