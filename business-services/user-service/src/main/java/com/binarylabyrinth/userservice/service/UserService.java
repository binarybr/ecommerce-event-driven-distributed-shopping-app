package com.binarylabyrinth.userservice.service;

import com.binarylabyrinth.userservice.dto.*;

import java.util.List;

public interface UserService {

    UserResponseDto registerUser(UserRegistrationDto registrationDto);

    AuthResponseDto loginUser(LoginRequestDto loginDto);

    UserResponseDto getUserById(Long id);

    UserResponseDto getUserByEmail(String email);

    UserResponseDto updateUser(Long id, UserResponseDto updateDto);

    void deleteUser(Long id);

    void verifyEmail(String token);

    /** List all users (admin only). */
    List<UserResponseDto> listAllUsers();
}
