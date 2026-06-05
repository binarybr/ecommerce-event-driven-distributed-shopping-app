package com.binarylabyrinth.userservice.dto;

public class AuthResponseDto {

    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private UserResponseDto user;

    public AuthResponseDto() {
    }

    public AuthResponseDto(String accessToken, String tokenType, Long expiresIn, UserResponseDto user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserResponseDto getUser() {
        return user;
    }

    public void setUser(UserResponseDto user) {
        this.user = user;
    }

    public static AuthResponseDtoBuilder builder() {
        return new AuthResponseDtoBuilder();
    }

    public static class AuthResponseDtoBuilder {
        private String accessToken;
        private String tokenType;
        private Long expiresIn;
        private UserResponseDto user;

        public AuthResponseDtoBuilder accessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        public AuthResponseDtoBuilder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public AuthResponseDtoBuilder expiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        public AuthResponseDtoBuilder user(UserResponseDto user) {
            this.user = user;
            return this;
        }

        public AuthResponseDto build() {
            return new AuthResponseDto(accessToken, tokenType, expiresIn, user);
        }
    }
}
