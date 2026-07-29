package com.example.labteamwork.service;

import com.example.labteamwork.dao.*;
import com.example.labteamwork.dto.request.QuestionAnswerRequest;
import com.example.labteamwork.dto.request.QuizCreateRequest;
import com.example.labteamwork.dto.request.QuizRateRequest;
import com.example.labteamwork.dto.request.QuizSolveRequest;
import com.example.labteamwork.dto.response.*;
import com.example.labteamwork.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizDao quizDao;
    private final QuestionDao questionDao;
    private final OptionDao optionDao;
    private final QuizResultDao quizResultDao;
    private final QuizRatingDao quizRatingDao;
    private final UserDao userDao;

    /**
     * 1. Создание новой викторины с вопросами и вариантами ответов
     */
    public QuizDetailResponseDto createQuiz(QuizCreateRequest request, String username) {
        log.info("Запрос на создание викторины '{}' от пользователя '{}'", request.getTitle(), username);

        User creator = getUserByUsername(username);

        // Маппим DTO запроса в доменные модели
        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .timeLimitSeconds(request.getTimeLimitSeconds())
                .creatorId(creator.getId())
                .questions(request.getQuestions().stream().map(qDto -> Question.builder()
                        .questionText(qDto.getQuestionText())
                        .options(qDto.getOptions().stream().map(oDto -> Option.builder()
                                .optionText(oDto.getOptionText())
                                .isCorrect(oDto.getIsCorrect())
                                .build()).collect(Collectors.toList()))
                        .build()
                ).collect(Collectors.toList()))
                .build();

        // Каскадное сохранение через QuizDao -> QuestionDao -> OptionDao
        Quiz savedQuiz = quizDao.save(quiz);
        log.info("Викторина '{}' успешно создана с ID {}", savedQuiz.getTitle(), savedQuiz.getId());

        return getQuizById(savedQuiz.getId());
    }

    /**
     * 2. Получение списка всех викторин (сводная информация)
     */
    public List<QuizSummaryResponseDto> getAllQuizzes() {
        log.info("Получение списка всех викторин");

        return quizDao.findAll().stream().map(quiz -> {
            User creator = userDao.findById(quiz.getCreatorId()).orElse(null);
            int questionsCount = questionDao.countByQuizId(quiz.getId());

            return QuizSummaryResponseDto.builder()
                    .id(quiz.getId())
                    .title(quiz.getTitle())
                    .description(quiz.getDescription())
                    .category(quiz.getCategory())
                    .timeLimitSeconds(quiz.getTimeLimitSeconds())
                    .questionsCount(questionsCount)
                    .creatorUsername(creator != null ? creator.getUsername() : "Неизвестно")
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 3. Получение детализации викторины БЕЗ флага isCorrect (для прохождения)
     */
    public QuizDetailResponseDto getQuizById(Long quizId) {
        log.info("Запрос информации о викторине с ID {}", quizId);

        Quiz quiz = getQuizEntityById(quizId);
        User creator = userDao.findById(quiz.getCreatorId()).orElse(null);

        // Безопасный маппинг вопросов и вариантов (без isCorrect)
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
                .creatorUsername(creator != null ? creator.getUsername() : "Неизвестно")
                .questions(questionDtos)
                .build();
    }

    /**
     * 4. Прохождение викторины, подсчет баллов и сохранение результата
     */
    public QuizResultDto solveQuiz(Long quizId, QuizSolveRequest request, String username) {
        log.info("Пользователь '{}' отправляет ответы на викторину ID {}", username, quizId);

        User user = getUserByUsername(username);
        Quiz quiz = getQuizEntityById(quizId);

        // Проверка: запрет на повторное прохождение
        if (quizResultDao.existsByUserIdAndQuizId(user.getId(), quizId)) {
            log.warn("Пользователь '{}' пытался повторно пройти викторину ID {}", username, quizId);
            throw new IllegalStateException("Вы уже проходили эту викторину. Повторное прохождение запрещено.");
        }

        // Преобразуем ответы пользователя в удобный Map <questionId, selectedOptionId>
        Map<Long, Long> userAnswersMap = request.getAnswers().stream()
                .collect(Collectors.toMap(QuestionAnswerRequest::getQuestionId, QuestionAnswerRequest::getSelectedOptionId));

        int correctAnswersCount = 0;
        List<Question> questions = quiz.getQuestions();

        // Сверяем выбор пользователя с БД
        for (Question question : questions) {
            Long selectedOptionId = userAnswersMap.get(question.getId());
            if (selectedOptionId != null) {
                Option selectedOption = optionDao.findById(selectedOptionId).orElse(null);
                if (selectedOption != null && selectedOption.getIsCorrect()) {
                    correctAnswersCount++;
                }
            }
        }

        // Сохраняем попытку в базу
        QuizResult result = QuizResult.builder()
                .userId(user.getId())
                .quizId(quizId)
                .score(correctAnswersCount)
                .completedAt(LocalDateTime.now())
                .build();

        quizResultDao.save(result);
        log.info("Пользователь '{}' завершил викторину ID {} с результатом {}/{}", username, quizId, correctAnswersCount, questions.size());

        // Используем вспомогательный метод для сборки DTO
        return buildQuizResultDto(quiz, result, questions.size());
    }

    /**
     * 5. Просмотр своего ранее полученного результата по викторине
     */
    public QuizResultDto getQuizResult(Long quizId, String username) {
        log.info("Пользователь '{}' запрашивает свои результаты по викторине ID {}", username, quizId);

        User user = getUserByUsername(username);
        Quiz quiz = getQuizEntityById(quizId);

        QuizResult result = quizResultDao.findByUserIdAndQuizId(user.getId(), quizId)
                .orElseThrow(() -> new IllegalArgumentException("Вы еще не проходили эту викторину"));

        int totalQuestions = questionDao.countByQuizId(quizId);

        // Используем вспомогательный метод для сборки DTO
        return buildQuizResultDto(quiz, result, totalQuestions);
    }

    /**
     * 6. Выставить оценку викторине (от 1 до 5)
     */
    public void rateQuiz(Long quizId, QuizRateRequest request, String username) {
        log.info("Пользователь '{}' пытается поставить оценку {} викторине ID {}", username, request.getRating(), quizId);

        User user = getUserByUsername(username);
        checkQuizExists(quizId);

        // Проверка: оценивать можно только пройденную викторину
        if (!quizResultDao.existsByUserIdAndQuizId(user.getId(), quizId)) {
            throw new IllegalStateException("Вы не можете оценить викторину, пока не пройдете её");
        }

        // Проверка: повторно оценивать нельзя
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

    /**
     * 7. Получить таблицу лидеров по конкретной викторине
     */
    public List<LeaderboardEntryDto> getLeaderboard(Long quizId) {
        log.info("Запрос таблицы лидеров для викторины ID {}", quizId);

        checkQuizExists(quizId);

        return quizResultDao.findLeaderboardByQuizId(quizId);
    }

    // =========================================================================
    // Вспомогательные приватные методы (Устранение дублирования кода)
    // =========================================================================

    /**
     * Ищет пользователя по логину.
     * Централизует логику проверки на существование пользователя.
     *
     * @param username логин пользователя
     * @return сущность User
     * @throws IllegalArgumentException если пользователь не найден в базе данных
     */
    private User getUserByUsername(String username) {
        return userDao.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }

    /**
     * Загружает викторину со всеми связями по ID.
     * Используется, когда для бизнес-логики нужны данные самой викторины (вопросы, описание).
     *
     * @param quizId ID викторины
     * @return сущность Quiz
     * @throws IllegalArgumentException если викторина с таким ID отсутствует
     */
    private Quiz getQuizEntityById(Long quizId) {
        return quizDao.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Викторина с ID " + quizId + " не найдена"));
    }

    /**
     * Проверяет существование викторины в базе без загрузки её тяжелых связей (вопросов и вариантов).
     * Оптимально для методов оценки или лидерборда, где сущность Quiz целиком не нужна.
     *
     * @param quizId ID викторины
     * @throws IllegalArgumentException если викторина не существует
     */
    private void checkQuizExists(Long quizId) {
        if (!quizDao.existsById(quizId)) {
            throw new IllegalArgumentException("Викторина с ID " + quizId + " не найдена");
        }
    }

    /**
     * Собирает и рассчитывает итоговый DTO с результатами прохождения.
     * Изолирует в себе математику расчета процентов правильных ответов и округления.
     *
     * @param quiz           сущность пройденной викторины
     * @param result         сущность результата пользователя (содержит кол-во правильных ответов)
     * @param totalQuestions общее количество вопросов в этой викторине
     * @return готовый QuizResultDto для отправки на клиент
     */
    private QuizResultDto buildQuizResultDto(Quiz quiz, QuizResult result, int totalQuestions) {
        int correctAnswers = result.getScore();
        int incorrectAnswers = totalQuestions - correctAnswers;

        // Защита от деления на ноль и расчет процента успешности
        double percentage = totalQuestions > 0 ? ((double) correctAnswers / totalQuestions) * 100 : 0.0;

        return QuizResultDto.builder()
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .totalQuestions(totalQuestions)
                .correctAnswers(correctAnswers)
                .incorrectAnswers(incorrectAnswers)
                .score(correctAnswers)
                .percentage(Math.round(percentage * 100.0) / 100.0) // Округление до 2-х знаков
                .completedAt(result.getCompletedAt())
                .build();
    }
}