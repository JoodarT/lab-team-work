package com.example.labteamwork.dao;

import com.example.labteamwork.entity.Quiz;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuizDao {

    private final JdbcTemplate jdbcTemplate;
    private final QuestionDao questionDao;

    private final RowMapper<Quiz> quizRowMapper = (rs, rowNum) -> Quiz.builder()
            .id(rs.getLong("id"))
            .title(rs.getString("title"))
            .description(rs.getString("description"))
            .category(rs.getString("category"))
            .timeLimitSeconds(rs.getObject("time_limit_seconds") != null ? rs.getInt("time_limit_seconds") : null)
            .creatorId(rs.getLong("creator_id"))
            .build();

    public Quiz save(Quiz quiz) {
        String sql = "INSERT INTO quizzes (title, description, category, time_limit_seconds, creator_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, quiz.getTitle());
            ps.setString(2, quiz.getDescription());
            ps.setString(3, quiz.getCategory());
            if (quiz.getTimeLimitSeconds() != null) {
                ps.setInt(4, quiz.getTimeLimitSeconds());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.setLong(5, quiz.getCreatorId());
            return ps;
        }, keyHolder);

        quiz.setId(keyHolder.getKey().longValue());

        if (quiz.getQuestions() != null) {
            quiz.getQuestions().forEach(question -> {
                question.setQuizId(quiz.getId());
                questionDao.save(question);
            });
        }

        return quiz;
    }

    public List<Quiz> findAll(int limit, int offset) {
        String sql = "SELECT * FROM quizzes";
        return jdbcTemplate.query(sql, quizRowMapper);
    }

    public Optional<Quiz> findById(Long id) {
        String sql = "SELECT * FROM quizzes WHERE id = ?";
        Optional<Quiz> quiz = jdbcTemplate.query(sql, quizRowMapper, id).stream().findFirst();
        quiz.ifPresent(q -> q.setQuestions(questionDao.findByQuizId(q.getId())));
        return quiz;
    }

    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM quizzes WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    public String getQuizCreatorUsername(Long userId) {
        String sql = "SELECT u.username FROM users u WHERE u.id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, userId);
    }
}