package com.binarylabyrinth.userservice.dto;

import java.time.LocalDateTime;

public class UserResponseDto {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private Boolean enabled;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public UserResponseDto() {
    }

    public UserResponseDto(Long id, String email, String firstName, String lastName, String phone,
                           String role, Boolean enabled, Boolean emailVerified, LocalDateTime createdAt,
                           LocalDateTime lastLoginAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.role = role;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public static UserResponseDtoBuilder builder() {
        return new UserResponseDtoBuilder();
    }

    public static class UserResponseDtoBuilder {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
        private String role;
        private Boolean enabled;
        private Boolean emailVerified;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;

        public UserResponseDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserResponseDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserResponseDtoBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserResponseDtoBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserResponseDtoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserResponseDtoBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserResponseDtoBuilder enabled(Boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public UserResponseDtoBuilder emailVerified(Boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public UserResponseDtoBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserResponseDtoBuilder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }

        public UserResponseDto build() {
            return new UserResponseDto(id, email, firstName, lastName, phone, role, enabled, emailVerified,
                    createdAt, lastLoginAt);
        }
    }
}
