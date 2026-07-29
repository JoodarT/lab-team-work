package com.example.labteamwork.service;

import com.example.labteamwork.dto.request.UserRegisterRequest;
import com.example.labteamwork.dto.response.UserResponseDto;
import com.example.labteamwork.dto.response.UserStatsDto;
import com.example.labteamwork.entity.User;
import java.util.List;

public interface UserService {

    UserResponseDto registerUser(UserRegisterRequest request);

    UserResponseDto getUserById(Long id);

    User getUserByEmail(String email);

    boolean existsByEmail(String email);

    List<UserResponseDto> getAllUsers();

    UserStatsDto getUserStatistics(Long userId);
}
