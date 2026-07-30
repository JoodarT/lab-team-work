package com.example.labteamwork.service;

import com.example.labteamwork.dto.request.QuizCreateRequest;
import com.example.labteamwork.dto.request.QuizRateRequest;
import com.example.labteamwork.dto.request.QuizSolveRequest;
import com.example.labteamwork.dto.response.*;

import java.util.List;

public interface QuizService {


    QuizDetailResponseDto createQuiz(QuizCreateRequest request, String username);

    List<QuizSummaryResponseDto> getAllQuizzes();

    QuizDetailResponseDto getQuizById(Long quizId);

    QuizResultDto solveQuiz(Long quizId, QuizSolveRequest request, String username);

    QuizResultDto getQuizResult(Long quizId, String username);

    void rateQuiz(Long quizId, QuizRateRequest request, String username);

    List<LeaderboardEntryDto> getLeaderboard(Long quizId);
}