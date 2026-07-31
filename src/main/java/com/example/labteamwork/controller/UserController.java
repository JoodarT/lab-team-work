package com.example.labteamwork.controller;

import com.example.labteamwork.dto.response.GlobalLeaderboardEntryDto;
import com.example.labteamwork.dto.response.UserStatsDto;
import com.example.labteamwork.service.UserStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Управление пользователями и получение статистики")
public class UserController {

    private final UserStatsService userStatsService;

    @Operation(summary = "Получение статистики пользователя по ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Статистика успешно получена"),
            @ApiResponse(responseCode = "404", description = "Пользователь с таким ID не найден")
    })
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<UserStatsDto> getUserStats(@PathVariable Long userId) {
        log.info("Запрос статистики для пользователя с ID: {}", userId);

        UserStatsDto stats = userStatsService.getUserStats(userId);

        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Получение глобальной таблицы лидеров",
            description = "Возвращает список лучших пользователей по сумме всех набранных баллов во всех викторинах")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Глобальный рейтинг успешно получен")
    })
    @GetMapping("/leaderboard")
    public ResponseEntity<List<GlobalLeaderboardEntryDto>> getGlobalLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Запрос глобального рейтинга лучших игроков, лимит: {}", limit);

        List<GlobalLeaderboardEntryDto> leaderboard = userStatsService.getGlobalLeaderboard(limit);

        return ResponseEntity.ok(leaderboard);
    }
}