package com.binarylabyrinth.productservice.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

/**
 * Explicit MongoClient configuration.
 *
 * Spring Boot 4's auto-configuration was ignoring spring.data.mongodb.uri from every
 * source (env var, JVM system property, profile YAML), always falling back to
 * localhost:27017. This config bypasses auto-config and reads the URI/database
 * directly from properties, making the connection explicit and predictable.
 *
 * Properties read:
 *   spring.data.mongodb.uri      (e.g., mongodb://mongo:27017/product_service)
 *   spring.data.mongodb.database (e.g., product_service)
 */
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/product_service}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:product_service}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    /**
     * Enable automatic creation of MongoDB indexes from entity annotations
     * (@Indexed, @TextIndexed, @CompoundIndex). Required for the search
     * feature — without this, $text queries fail with IndexNotFound.
     */
    @Override
    protected boolean autoIndexCreation() {
        return true;
    }

    @Override
    @Bean
    @Primary
    public MongoClient mongoClient() {
        System.out.println("[MongoConfig] Connecting to MongoDB at: " + mongoUri);
        return MongoClients.create(mongoUri);
    }
}
