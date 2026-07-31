package com.example.labteamwork.controller;

import com.example.labteamwork.dto.request.QuizCreateRequest;
import com.example.labteamwork.dto.request.QuizRateRequest;
import com.example.labteamwork.dto.request.QuizSolveRequest;
import com.example.labteamwork.dto.response.*;
import com.example.labteamwork.entity.User;
import com.example.labteamwork.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "Управление квизами, прохождением и рейтингом")
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "Создание нового квиза")
    @PostMapping
    public ResponseEntity<QuizDetailResponseDto> createQuiz(
            @Valid @RequestBody QuizCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос на создание квиза от пользователя: {}", userDetails.getUsername());
        QuizDetailResponseDto response = quizService.createQuiz(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Начать прохождение квиза (запуск таймера)")
    @PostMapping("/{quizId}/start")
    public ResponseEntity<QuizStartResponseDto> startQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal User userDetails) {

        log.info("Запрос на старт квиза {} от пользователя с ID: {}", quizId, userDetails.getId());

        QuizStartResponseDto response = quizService.startQuiz(quizId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Получение списка всех квизов")
    @GetMapping
    public ResponseEntity<List<QuizSummaryResponseDto>> getAllQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение списка всех квизов");
        return ResponseEntity.ok(quizService.getAllQuizzes(page, size));
    }

    @Operation(summary = "Получение детальной информации о квизе по ID")
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizDetailResponseDto> getQuizById(@PathVariable Long quizId) {
        log.info("Запрос на получение квиза с ID: {}", quizId);
        return ResponseEntity.ok(quizService.getQuizById(quizId));
    }

    @Operation(summary = "Прохождение квиза (отправка ответов)")
    @PostMapping("/{quizId}/solve")
    public ResponseEntity<QuizResultDto> solveQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSolveRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Пользователь {} проходил квиз с ID: {}", userDetails.getUsername(), quizId);
        QuizResultDto result = quizService.solveQuiz(quizId, request, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Получение результата прохождения квиза пользователем")
    @GetMapping("/{quizId}/results")
    public ResponseEntity<QuizResultDto> getQuizResult(
            @PathVariable Long quizId,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Запрос результата квиза {} для пользователя {}", quizId, userDetails.getUsername());
        QuizResultDto result = quizService.getQuizResult(quizId, userDetails.getUsername());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Оценить квиз")
    @PostMapping("/{quizId}/rate")
    public ResponseEntity<Void> rateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizRateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("Оценка квиза {} пользователем {}", quizId, userDetails.getUsername());
        quizService.rateQuiz(quizId, request, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получение таблицы лидеров для квиза")
    @GetMapping("/{quizId}/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDto>> getLeaderboard(@PathVariable Long quizId) {
        log.info("Запрос таблицы лидеров для квиза ID: {}", quizId);
        return ResponseEntity.ok(quizService.getLeaderboard(quizId));
    }
}