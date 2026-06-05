package com.binarylabyrinth.userservice.mapper;

import com.binarylabyrinth.userservice.dto.UserResponseDto;
import com.binarylabyrinth.userservice.entity.User;
import org.springframework.stereotype.Component;

/**
 * UserMapper - User entity ↔ DTO conversions.
 *
 * toResponseDto deliberately OMITS the password hash and verification token —
 * those must never leave the service.
 */
@Component
public class UserMapper {

    /** Entity → safe response DTO (no password / no verification token). */
    public UserResponseDto toResponseDto(User user) {
        if (user == null) {
            return null;
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    /**
     * Apply an update onto an existing user. Only the profile fields
     * (firstName/lastName/phone) are mutable here — email, role, password, and
     * enabled status are intentionally NOT updatable via this path to prevent
     * privilege escalation or account takeover through a profile edit.
     */
    public User toEntity(User user, UserResponseDto dto) {
        if (dto == null) {
            return user;
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhone(dto.getPhone());

        return user;
    }
}
