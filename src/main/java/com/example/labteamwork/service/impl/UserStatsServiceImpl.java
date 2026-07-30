package com.example.labteamwork.service.impl;

import com.example.labteamwork.dao.QuizResultDao;
import com.example.labteamwork.dao.UserDao;
import com.example.labteamwork.dto.response.UserStatsDto;
import com.example.labteamwork.entity.User;
import com.example.labteamwork.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final QuizResultDao quizResultDao;
    private final UserDao userDao;

    /**
     * Получение статистики о достижениях пользователя по его ID.
     * Реализует эндпоинт GET /api/users/{userId}/statistics
     */
    @Override
    public UserStatsDto getUserStats(Long userId) {
        log.info("Запрос статистики для пользователя с ID {}", userId);

        User user = userDao.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с ID " + userId + " не найден"));

        return quizResultDao.getUserStats(user.getId())
                .orElseGet(() -> buildDefaultStats(user));
    }

    /**
     * Формирование дефолтной статистики для пользователя, у которого ещё нет пройденных викторин.
     */
    private UserStatsDto buildDefaultStats(User user) {
        return UserStatsDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .totalQuizzesTaken(0)
                .totalQuizzesCreated(0)
                .totalScore(0)
                .averagePercentage(0.0)
                .build();
    }
}