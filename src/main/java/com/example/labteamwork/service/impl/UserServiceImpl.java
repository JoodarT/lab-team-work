package com.example.labteamwork.service.impl;

import com.example.labteamwork.dao.UserDao;
import com.example.labteamwork.dto.request.UserRegisterRequest;
import com.example.labteamwork.dto.response.UserResponseDto;
import com.example.labteamwork.dto.response.UserStatsDto;
import com.example.labteamwork.entity.User;
import com.example.labteamwork.service.UserService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userDao.existsByEmail(email);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return List.of();
    }

    @Override
    public UserStatsDto getUserStatistics(Long userId) {
        return null;
    }

    @Override
    public UserResponseDto registerUser(UserRegisterRequest request) {
        return null;
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        return null;
    }

    @Override
    public User getUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

}
