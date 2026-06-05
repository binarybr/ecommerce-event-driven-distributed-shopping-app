package com.binarylabyrinth.userservice.dto;

import jakarta.validation.constraints.*;

public class LoginRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequestDto() {
    }

    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
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

    public static LoginRequestDtoBuilder builder() {
        return new LoginRequestDtoBuilder();
    }

    public static class LoginRequestDtoBuilder {
        private String email;
        private String password;

        public LoginRequestDtoBuilder email(String email) {
            this.email = email;
            return this;
        }

        public LoginRequestDtoBuilder password(String password) {
            this.password = password;
            return this;
        }

        public LoginRequestDto build() {
            return new LoginRequestDto(email, password);
        }
    }
}
