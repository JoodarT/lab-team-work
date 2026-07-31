package com.example.labteamwork.controller;

import com.example.labteamwork.dto.request.UserLoginRequest;
import com.example.labteamwork.dto.request.UserRegisterRequest;
import com.example.labteamwork.dto.response.UserResponseDto;
import com.example.labteamwork.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Эндпоинты для регистрации и аутентификации пользователей")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации входных данных или пользователь уже существует")
    })

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("Получен запрос на регистрацию пользователя с username: {}", request.getUsername());

        authService.registerUser(request);

        log.info("Пользователь {} успешно зарегистрирован", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Аутентификация пользователя (Вход)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Получен запрос на вход пользователя: {}", request.getUsername());

        UserResponseDto response = authService.login(request);

        log.info("Пользователь {} успешно вошел в систему", request.getUsername());
        return ResponseEntity.ok(response);
    }
}