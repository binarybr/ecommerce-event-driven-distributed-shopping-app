package com.binarylabyrinth.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * ConfigServerApplication - Spring Cloud Config Server
 *
 * This is the centralized configuration management server for the microservices.
 * All microservices can fetch their configuration from this server.
 *
 * RESPONSIBILITIES:
 * 1. Centralized Configuration: Single source of truth for configs
 * 2. Environment-specific Properties: Dev, Staging, Production configs
 * 3. Dynamic Updates: Update configs without restarting services
 * 4. Version Control: Configuration stored in Git repository
 * 5. Access Control: Secure access to sensitive configurations
 *
 * ARCHITECTURE:
 * ┌─────────────────────────────────────┐
 * │  Spring Cloud Config Server         │
 * │  (Port 8888)                        │
 * │  http://localhost:8888             │
 * └─────────────────────────────────────┘
 *              ↓ Serves configs from
 * ┌─────────────────────────────────────┐
 * │  Git Repository                     │
 * │  (https://github.com/your-org/..)   │
 * │  ├── application.yml (default)      │
 * │  ├── application-dev.yml            │
 * │  ├── application-staging.yml        │
 * │  └── application-prod.yml           │
 * └─────────────────────────────────────┘
 *
 * CLIENTS FETCH CONFIGS VIA:
 * - REST API: http://localhost:8888/{service}/{profile}/master
 * - Spring Boot: spring.config.import=configserver:http://localhost:8888
 *
 * CONFIGURATION (application.yaml):
 * - server.port: 8888
 * - spring.cloud.config.server.git.uri: Git repository URL
 * - spring.cloud.config.server.git.default-label: main/master
 *
 * FEATURES:
 * 1. Externalized Configuration: Properties outside application code
 * 2. Profile-based Configuration: Different configs per environment
 * 3. Property Refreshing: @RefreshScope annotations for dynamic updates
 * 4. Encryption: Support for encrypting sensitive properties
 * 5. Versioning: Git provides configuration history and rollback
 *
 * SERVICE INTEGRATION:
 * Each service needs in bootstrap.yml:
 * ```
 * spring:
 *   config:
 *     import: optional:configserver:http://localhost:8888
 * ```
 *
 * SERVICE CONFIGURATION HIERARCHY:
 * 1. application.yaml (local - hardcoded defaults)
 * 2. bootstrap.yml (early bootstrap)
 * 3. Config Server (centralized - overrides local)
 * 4. Environment Variables (runtime - highest priority)
 *
 * EXAMPLE USAGE:
 * // In service:
 * @Value("${database.host}")
 * private String databaseHost;  // Fetched from Config Server
 *
 * @EnableConfigServer - Activates Config server functionality
 *
 * @author Binary Labyrinth
 * @version 1.0
 */
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    /**
     * Main entry point for Config Server application
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }

}
