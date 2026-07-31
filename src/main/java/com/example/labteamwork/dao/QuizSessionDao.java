package com.example.labteamwork.dao;

import com.example.labteamwork.entity.QuizSession;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuizSessionDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<QuizSession> sessionRowMapper = (rs, rowNum) -> QuizSession.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .quizId(rs.getLong("quiz_id"))
            .startedAt(rs.getTimestamp("started_at").toLocalDateTime())
            .build();

    public void saveOrUpdate(Long userId, Long quizId, LocalDateTime startedAt) {
        String sql = """
            MERGE INTO quiz_sessions (user_id, quiz_id, started_at)
            KEY(user_id, quiz_id)
            VALUES (?, ?, ?)
            """;
        jdbcTemplate.update(sql, userId, quizId, startedAt);
    }

    public Optional<QuizSession> findByUserIdAndQuizId(Long userId, Long quizId) {
        String sql = "SELECT * FROM quiz_sessions WHERE user_id = ? AND quiz_id = ?";
        return jdbcTemplate.query(sql, sessionRowMapper, userId, quizId)
                .stream()
                .findFirst();
    }

    public void delete(Long userId, Long quizId) {
        String sql = "DELETE FROM quiz_sessions WHERE user_id = ? AND quiz_id = ?";
        jdbcTemplate.update(sql, userId, quizId);
    }
}