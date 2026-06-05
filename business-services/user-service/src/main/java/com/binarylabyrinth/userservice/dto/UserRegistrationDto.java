package com.binarylabyrinth.userservice.dto;

import jakarta.validation.constraints.*;

/**
 * UserRegistrationDto - body for POST /api/users/register.
 *
 * Bean Validation runs before the controller method; any violation returns a
 * 400 with field-level messages (handled by GlobalExceptionHandler). All
 * constraints below must pass or registration is rejected.
 */
public class UserRegistrationDto {

    /** Must be a syntactically valid, unique email (also the login id). */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    /** Minimum 8 chars; stored only as a BCrypt hash, never in plaintext. */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    /** Digits only, 10-15 long. NOTE: a 9-digit value is rejected with 400. */
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    private String phone;

    /**
     * Optional admin registration key. When provided and matching the
     * ADMIN_REGISTRATION_KEY environment variable the account is created with
     * role ADMIN. Omitting it (or sending null / blank) always yields CUSTOMER.
     * This field is intentionally NOT stored and is never logged.
     */
    private String adminKey;

    public UserRegistrationDto() {
    }

    public UserRegistrationDto(String email, String password, String firstName, String lastName, String phone) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getAdminKey() {
        return adminKey;
    }

    public void setAdminKey(String adminKey) {
        this.adminKey = adminKey;
    }

    public static UserRegistrationDtoBuilder builder() {
        return new UserRegistrationDtoBuilder();
    }

    public static class UserRegistrationDtoBuilder {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phone;

        public UserRegistrationDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserRegistrationDtoBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserRegistrationDtoBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public UserRegistrationDtoBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public UserRegistrationDtoBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public UserRegistrationDto build() {
            return new UserRegistrationDto(email, password, firstName, lastName, phone);
        }
    }
}
