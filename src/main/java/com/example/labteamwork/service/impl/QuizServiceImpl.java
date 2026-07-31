package com.example.labteamwork.service.impl;

import com.example.labteamwork.dao.*;
import com.example.labteamwork.dto.request.QuestionAnswerRequest;
import com.example.labteamwork.dto.request.QuizCreateRequest;
import com.example.labteamwork.dto.request.QuizRateRequest;
import com.example.labteamwork.dto.request.QuizSolveRequest;
import com.example.labteamwork.dto.response.*;
import com.example.labteamwork.entity.*;
import com.example.labteamwork.exception.ResourceNotFoundException;
import com.example.labteamwork.service.QuizService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizDao quizDao;
    private final QuestionDao questionDao;
    private final OptionDao optionDao;
    private final QuizResultDao quizResultDao;
    private final QuizRatingDao quizRatingDao;
    private final UserDao userDao;
    private final QuizSessionDao quizSessionDao;

    @Override
    public QuizDetailResponseDto createQuiz(QuizCreateRequest request, String username) {
        log.info("Запрос на создание викторины '{}' от пользователя '{}'", request.getTitle(), username);
        User creator = getUserByUsername(username);

        Quiz quiz = mapToQuizEntity(request, creator.getId());

        Quiz savedQuiz = quizDao.save(quiz);
        log.info("Викторина '{}' успешно создана с ID {}", savedQuiz.getTitle(), savedQuiz.getId());

        return getQuizById(savedQuiz.getId());
    }

    @Override
    public QuizStartResponseDto startQuiz(Long quizId, Long userId) {
        log.info("Пользователь с id {} начинает викторину id {}", userId, quizId);

        Quiz quiz = quizDao.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Викторина с id " + quizId + " не найдена"));

        if (quizResultDao.existsByUserIdAndQuizId(userId, quizId)) {
            throw new IllegalArgumentException("Вы уже проходили эту викторину!");
        }

        LocalDateTime now = LocalDateTime.now();
        quizSessionDao.saveOrUpdate(userId, quizId, now);

        return QuizStartResponseDto.builder()
                .quizId(quizId)
                .startedAt(now)
                .timeLimitSeconds(quiz.getTimeLimitSeconds())
                .message("Викторина успешно начата. Отсчет времени пошел!")
                .build();
    }

    @Override
    public List<QuizSummaryResponseDto> getAllQuizzes(String category, int page, int size) {
        log.info("Запрос списка квизов. Страница: {}, размер: {}", page, size);

        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        int offset = page * size;

        List<Quiz> quizzes = quizDao.findAll(category, size, offset);

        return quizzes.stream()
                .map( quiz -> mapToQuizSummaryDto(
                        quiz,
                        quizDao.getQuizCreatorUsername(quiz.getCreatorId()),
                        questionDao.countByQuizId(quiz.getId())))
                .toList();
    }

    @Override
    public QuizDetailResponseDto getQuizById(Long quizId) {
        log.info("Запрос информации о викторине с ID {}", quizId);

        Quiz quiz = getQuizEntityById(quizId);
        String creatorUsername = getCreatorUsernameById(quiz.getCreatorId());

        return mapToQuizDetailDto(quiz, creatorUsername);
    }

    @Override
    public QuizResultDto solveQuiz(Long quizId, QuizSolveRequest request, String username) {
        log.info("Пользователь '{}' отправляет ответы на викторину ID {}", username, quizId);

        User user = getUserByUsername(username);
        Quiz quiz = getQuizEntityById(quizId);

        if (quizResultDao.existsByUserIdAndQuizId(user.getId(), quizId)) {
            log.warn("Пользователь '{}' пытался повторно пройти викторину ID {}", username, quizId);
            throw new IllegalStateException("Вы уже проходили эту викторину. Повторное прохождение запрещено.");
        }

        if (quiz.getTimeLimitSeconds() != null && quiz.getTimeLimitSeconds() > 0) {
            QuizSession session = quizSessionDao.findByUserIdAndQuizId(user.getId(), quizId)
                    .orElseThrow(() -> new IllegalArgumentException("Сначала необходимо начать викторину через /api/quizzes/" + quizId + "/start"));

            long secondsElapsed = java.time.Duration.between(session.getStartedAt(), LocalDateTime.now()).getSeconds();

            int allowedTime = quiz.getTimeLimitSeconds() + 5;

            if (secondsElapsed > allowedTime) {
                quizSessionDao.delete(user.getId(), quizId);
                throw new IllegalArgumentException("Время на прохождение викторины истекло! Вы потратили "
                        + secondsElapsed + " сек. из разрешенных " + quiz.getTimeLimitSeconds() + " сек.");
            }
        }

        Map<Long, Long> userAnswersMap = request.getAnswers().stream()
                .collect(Collectors.toMap(QuestionAnswerRequest::getQuestionId, QuestionAnswerRequest::getSelectedOptionId));

        int correctAnswersCount = 0;
        List<Question> questions = quiz.getQuestions();

        for (Question question : questions) {
            Long selectedOptionId = userAnswersMap.get(question.getId());
            if (selectedOptionId != null) {
                Option selectedOption = optionDao.findById(selectedOptionId).orElse(null);
                if (selectedOption != null && selectedOption.getIsCorrect()) {
                    correctAnswersCount++;
                }
            }
        }
        QuizResult result = QuizResult.builder()
                .userId(user.getId())
                .quizId(quizId)
                .score(correctAnswersCount)
                .completedAt(LocalDateTime.now())
                .build();

        quizResultDao.save(result);
        quizSessionDao.delete(user.getId(), quizId);
        log.info("Пользователь '{}' завершил викторину ID {} с результатом {}/{}", username, quizId, correctAnswersCount, questions.size());

        return buildQuizResultDto(quiz, result, questions.size());
    }

    @Override
    public QuizResultDto getQuizResult(Long quizId, String username) {
        log.info("Пользователь '{}' запрашивает свои результаты по викторине ID {}", username, quizId);

        User user = getUserByUsername(username);
        Quiz quiz = getQuizEntityById(quizId);

        QuizResult result = quizResultDao.findByUserIdAndQuizId(user.getId(), quizId)
                .orElseThrow(() -> new IllegalArgumentException("Вы еще не проходили эту викторину"));

        int totalQuestions = questionDao.countByQuizId(quizId);

        return buildQuizResultDtoWithAnswers(quiz, result, totalQuestions);
    }

    @Override
    public void rateQuiz(Long quizId, QuizRateRequest request, String username) {
        log.info("Пользователь '{}' пытается поставить оценку {} викторине ID {}", username, request.getRating(), quizId);

        User user = getUserByUsername(username);
        checkQuizExists(quizId);

        if (!quizResultDao.existsByUserIdAndQuizId(user.getId(), quizId)) {
            throw new IllegalStateException("Вы не можете оценить викторину, пока не пройдете её");
        }

        if (quizRatingDao.existsByUserIdAndQuizId(user.getId(), quizId)) {
            throw new IllegalStateException("Вы уже выставили оценку этой викторине");
        }

        QuizRating rating = QuizRating.builder()
                .userId(user.getId())
                .quizId(quizId)
                .rating(request.getRating())
                .build();

        quizRatingDao.save(rating);
        log.info("Пользователь '{}' успешно поставил оценку {} викторине ID {}", username, request.getRating(), quizId);
    }

    @Override
    public List<LeaderboardEntryDto> getLeaderboard(Long quizId) {
        log.info("Запрос таблицы лидеров для викторины ID {}", quizId);

        checkQuizExists(quizId);

        return quizResultDao.findLeaderboardByQuizId(quizId);
    }

    private User getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    private Quiz getQuizEntityById(Long quizId) {
        return quizDao.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Викторина с ID " + quizId + " не найдена"));
    }

    private void checkQuizExists(Long quizId) {
        if (!quizDao.existsById(quizId)) {
            throw new IllegalArgumentException("Викторина с ID " + quizId + " не найдена");
        }
    }

    private String getCreatorUsernameById(Long creatorId) {
        return userDao.findById(creatorId)
                .map(User::getUsername)
                .orElse("Неизвестно");
    }

    private Quiz mapToQuizEntity(QuizCreateRequest request, Long creatorId) {
        return Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .creatorId(creatorId)
                .questions(request.getQuestions().stream().map(qDto -> Question.builder()
                        .questionText(qDto.getQuestionText())
                        .options(qDto.getOptions().stream().map(oDto -> Option.builder()
                                .optionText(oDto.getOptionText())
                                .isCorrect(oDto.getIsCorrect())
                                .build()).collect(Collectors.toList()))
                        .build()
                ).collect(Collectors.toList()))
                .build();
    }

    private QuizSummaryResponseDto mapToQuizSummaryDto(Quiz quiz, String creatorUsername, int questionsCount) {
        return QuizSummaryResponseDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .category(quiz.getCategory())
                .timeLimitSeconds(quiz.getTimeLimitSeconds())
                .questionsCount(questionsCount)
                .creatorUsername(creatorUsername)
                .build();
    }

    private QuizDetailResponseDto mapToQuizDetailDto(Quiz quiz, String creatorUsername) {
        List<QuestionResponseDto> questionDtos = quiz.getQuestions().stream().map(q -> {
            List<OptionResponseDto> optionDtos = q.getOptions().stream()
                    .map(o -> OptionResponseDto.builder()
                            .id(o.getId())
                            .optionText(o.getOptionText())
                            .build())
                    .collect(Collectors.toList());

            return QuestionResponseDto.builder()
                    .id(q.getId())
                    .questionText(q.getQuestionText())
                    .options(optionDtos)
                    .build();
        }).collect(Collectors.toList());

        return QuizDetailResponseDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .category(quiz.getCategory())
                .timeLimitSeconds(quiz.getTimeLimitSeconds())
                .creatorUsername(creatorUsername)
                .questions(questionDtos)
                .build();
    }

    private QuizResultDto buildQuizResultDto(Quiz quiz, QuizResult result, int totalQuestions) {
        int correctAnswers = result.getScore();
        int incorrectAnswers = totalQuestions - correctAnswers;

        double percentage = totalQuestions > 0 ? ((double) correctAnswers / totalQuestions) * 100 : 0.0;

        return QuizResultDto.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .incorrectAnswers(incorrectAnswers)
                .score(correctAnswers)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .completedAt(result.getCompletedAt())
                .build();
    }

    private QuizResultDto buildQuizResultDtoWithAnswers(Quiz quiz, QuizResult result, int totalQuestions) {
        int correctAnswers = result.getScore();
        int incorrectAnswers = totalQuestions - correctAnswers;
        double percentage = totalQuestions > 0 ? ((double) correctAnswers / totalQuestions) * 100 : 0.0;

        List<QuestionDetailResultDto> questionDtos = quiz.getQuestions().stream().map(q -> {
            List<OptionDetailResponseDto> optionDtos = q.getOptions().stream()
                    .map(o -> OptionDetailResponseDto.builder()
                            .id(o.getId())
                            .optionText(o.getOptionText())
                            .isCorrect(o.getIsCorrect())
                            .build())
                    .collect(Collectors.toList());

            return QuestionDetailResultDto.builder()
                    .id(q.getId())
                    .questionText(q.getQuestionText())
                    .options(optionDtos)
                    .build();
        }).collect(Collectors.toList());

        return QuizResultDto.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .incorrectAnswers(incorrectAnswers)
                .score(correctAnswers)
                .percentage(Math.round(percentage * 100.0) / 100.0)
                .completedAt(result.getCompletedAt())
                .questionsWithAnswers(questionDtos)
                .build();
    }
}