package com.binarylabyrinth.userservice.service.impl;

import com.binarylabyrinth.message.UserRegisteredEvent;
import com.binarylabyrinth.userservice.dto.*;
import com.binarylabyrinth.userservice.entity.User;
import com.binarylabyrinth.userservice.exception.UserException;
import com.binarylabyrinth.userservice.exception.UserNotFoundException;
import com.binarylabyrinth.userservice.mapper.UserMapper;
import com.binarylabyrinth.userservice.repository.UserRepository;
import com.binarylabyrinth.userservice.security.JwtUtil;
import com.binarylabyrinth.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserServiceImpl - authentication & user-management business logic.
 *
 * Security model:
 *   - Passwords are BCrypt-hashed at registration; the plaintext is never
 *     stored or logged.
 *   - On login we issue a stateless JWT (subject=email, claims: userId, role)
 *     that every other service validates with the shared HMAC secret.
 *   - New users default to role CUSTOMER; promotion to ADMIN is a manual/DB
 *     operation (no self-service privilege escalation).
 *
 * On registration a "user-registered" Kafka event is published so
 * notification-service can send a welcome email asynchronously — registration
 * does not block on email delivery.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, JwtUtil jwtUtil,
                           PasswordEncoder passwordEncoder, KafkaTemplate<String, Object> kafkaTemplate) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public UserResponseDto registerUser(UserRegistrationDto registrationDto) {
        log.info("Registering new user: {}", registrationDto.getEmail());

        // Reject duplicate emails up-front (email is also a unique column, so
        // this is a friendly check ahead of the DB constraint).
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new UserException("Email already registered");
        }

        // Determine role: ADMIN only when the caller supplies the correct
        // ADMIN_REGISTRATION_KEY env-var value; everything else → CUSTOMER.
        String role = resolveRole(registrationDto.getAdminKey());
        log.info("Registering user {} with role {}", registrationDto.getEmail(), role);

        User user = User.builder()
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword())) // never store plaintext
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .phone(registrationDto.getPhone())
                .role(role)
                .enabled(true)
                .emailVerified(false)        // until the verification token is used
                .verificationToken(UUID.randomUUID().toString())
                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))  // 24h to verify
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getId());

        publishUserRegisteredEvent(savedUser);

        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public AuthResponseDto loginUser(LoginRequestDto loginDto) {
        log.info("Login attempt for user: {}", loginDto.getEmail());

        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + loginDto.getEmail()));

        if (!user.getEnabled()) {
            throw new UserException("User account is disabled");
        }

        // BCrypt.matches re-hashes the candidate with the stored salt and
        // compares — constant-time, no plaintext comparison.
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new UserException("Invalid email or password");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Token carries userId + role as claims so downstream services can
        // authorize without calling back to user-service.
        String token = jwtUtil.generateToken(user.getEmail(), user.getId(), user.getRole());

        log.info("User logged in successfully: {}", user.getId());

        return AuthResponseDto.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationTime())
                .user(userMapper.toResponseDto(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        log.debug("Fetching user by ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));

        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        log.debug("Fetching user by email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with email: " + email));

        return userMapper.toResponseDto(user);
    }

    @Override
    public UserResponseDto updateUser(Long id, UserResponseDto updateDto) {
        log.info("Updating user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));

        userMapper.toEntity(user, updateDto);
        User updatedUser = userRepository.save(user);

        log.info("User updated successfully: {}", id);

        return userMapper.toResponseDto(updatedUser);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + id));

        userRepository.delete(user);

        log.info("User deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.List<UserResponseDto> listAllUsers() {
        log.debug("Listing all users (admin)");
        return userRepository.findAll().stream()
                .map(userMapper::toResponseDto)
                .toList();
    }

    @Override
    public void verifyEmail(String token) {
        log.info("Verifying email with token");

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new UserException("Invalid verification token"));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new UserException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        log.info("Email verified successfully for user: {}", user.getId());
    }

    /**
     * Returns "ADMIN" if the supplied key is non-blank and exactly matches the
     * ADMIN_REGISTRATION_KEY environment variable; otherwise returns "CUSTOMER".
     * A missing or blank env-var means admin self-registration is disabled.
     */
    private String resolveRole(String suppliedKey) {
        if (suppliedKey == null || suppliedKey.isBlank()) return "CUSTOMER";
        String configuredKey = System.getenv("ADMIN_REGISTRATION_KEY");
        if (configuredKey == null || configuredKey.isBlank()) {
            throw new UserException("Admin registration is not enabled on this server");
        }
        if (!suppliedKey.equals(configuredKey)) {
            throw new UserException("Invalid admin registration key");
        }
        return "ADMIN";
    }

    private void publishUserRegisteredEvent(User user) {
        try {
            UserRegisteredEvent event = UserRegisteredEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole())
                    .registeredAt(LocalDateTime.now())
                    .build();

            kafkaTemplate.send("user-registered", event);
            log.info("User registered event published for user: {}", user.getId());
        } catch (Exception ex) {
            log.error("Error publishing user registered event", ex);
        }
    }
}
