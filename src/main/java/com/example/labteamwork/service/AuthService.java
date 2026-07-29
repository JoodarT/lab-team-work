package com.example.labteamwork.service;

import com.example.labteamwork.dao.UserDao;
import com.example.labteamwork.dto.request.UserRegisterRequest;
import com.example.labteamwork.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(UserRegisterRequest request) {
        log.info("Попытка регистрации нового пользователя с username: {}", request.getUsername());

        if (userDao.existsByUsername(request.getUsername())) {
            log.warn("Ошибка регистрации: пользователь с username '{}' уже существует", request.getUsername());
            throw new IllegalArgumentException("Пользователь с таким именем уже существует");
        }

        if (userDao.existsByEmail(request.getEmail())) {
            log.warn("Ошибка регистрации: email '{}' уже используется", request.getEmail());
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .build();

        User savedUser = userDao.save(user);

        userDao.addRoleToUser(savedUser.getId(), "ROLE_USER");

        log.info("Пользователь '{}' успешно зарегистрирован с ID {}", savedUser.getUsername(), savedUser.getId());
    }
}