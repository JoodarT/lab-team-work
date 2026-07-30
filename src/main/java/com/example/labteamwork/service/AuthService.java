package com.example.labteamwork.service;

import com.example.labteamwork.dto.request.UserLoginRequest;
import com.example.labteamwork.dto.request.UserRegisterRequest;
import com.example.labteamwork.dto.response.UserResponseDto;

public interface AuthService {

    void registerUser(UserRegisterRequest request);
    UserResponseDto login(UserLoginRequest request);
}