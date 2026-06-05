package com.binarylabyrinth.userservice.config;

import com.binarylabyrinth.userservice.entity.User;
import com.binarylabyrinth.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * DataSeeder — runs once at startup to ensure a default admin account exists.
 *
 * Credentials are pulled from environment variables so they can be changed
 * per-environment without rebuilding the image:
 *
 *   ADMIN_EMAIL     (default: admin@shopsphere.com)
 *   ADMIN_PASSWORD  (default: Admin@1234)
 *   ADMIN_FIRST     (default: Shop)
 *   ADMIN_LAST      (default: Admin)
 *   ADMIN_PHONE     (default: 0000000000)
 *
 * The seeder is idempotent — if an account with ADMIN_EMAIL already exists
 * it does nothing, so re-deploying never resets a changed password.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String email    = env("ADMIN_EMAIL",    "admin@shopsphere.com");
        String password = env("ADMIN_PASSWORD", "Admin@1234");
        String firstName = env("ADMIN_FIRST",   "Shop");
        String lastName  = env("ADMIN_LAST",    "Admin");
        String phone     = env("ADMIN_PHONE",   "0000000000");

        if (userRepository.existsByEmail(email)) {
            log.info("Admin account already exists — skipping seed ({})", email);
            return;
        }

        User admin = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .role("ADMIN")
                .enabled(true)
                .emailVerified(true)   // admin needs no email verification
                .build();

        userRepository.save(admin);
        log.info("Default admin account created: {}", email);
    }

    private static String env(String key, String fallback) {
        String val = System.getenv(key);
        return (val != null && !val.isBlank()) ? val : fallback;
    }
}
