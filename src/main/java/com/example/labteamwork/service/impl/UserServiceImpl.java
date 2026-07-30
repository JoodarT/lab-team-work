package com.example.labteamwork.service.impl;

import com.example.labteamwork.dao.UserDao;
import com.example.labteamwork.dto.request.UserRegisterRequest;
import com.example.labteamwork.dto.response.UserResponseDto;
import com.example.labteamwork.dto.response.UserStatsDto;
import com.example.labteamwork.entity.User;
import com.example.labteamwork.service.UserService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

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
    public User getUserByEmail(String email) {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь с email " + email + " не найден"));
    }

    @Override
    public UserResponseDto registerUser(UserRegisterRequest request) {
        if (userDao.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setEnabled(true);

        User savedUser = userDao.save(user);

        userDao.addRoleToUser(savedUser.getId(), "ROLE_USER");

        return mapToResponseDto(savedUser);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь с id " + id + " не найден"));
        return mapToResponseDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userDao.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserStatsDto getUserStatistics(Long userId) {
        return null;
    }




    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }
}
