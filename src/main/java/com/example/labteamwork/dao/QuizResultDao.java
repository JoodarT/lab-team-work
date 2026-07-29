package com.example.labteamwork.dao;

import com.example.labteamwork.dto.response.LeaderboardEntryDto;
import com.example.labteamwork.dto.response.UserStatsDto;
import com.example.labteamwork.entity.QuizResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class QuizResultDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<QuizResult> resultRowMapper = (rs, rowNum) -> QuizResult.builder()
            .id(rs.getLong("id"))
            .userId(rs.getLong("user_id"))
            .quizId(rs.getLong("quiz_id"))
            .score(rs.getInt("score"))
            .completedAt(rs.getTimestamp("completed_at").toLocalDateTime())
            .build();

    public QuizResult save(QuizResult result) {
        String sql = "INSERT INTO quiz_results (user_id, quiz_id, score, completed_at) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, result.getUserId());
            ps.setLong(2, result.getQuizId());
            ps.setInt(3, result.getScore());
            ps.setTimestamp(4, Timestamp.valueOf(result.getCompletedAt()));
            return ps;
        }, keyHolder);

        result.setId(keyHolder.getKey().longValue());
        return result;
    }

    public boolean existsByUserIdAndQuizId(Long userId, Long quizId) {
        String sql = "SELECT COUNT(*) FROM quiz_results WHERE user_id = ? AND quiz_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, quizId);
        return count != null && count > 0;
    }

    public Optional<QuizResult> findByUserIdAndQuizId(Long userId, Long quizId) {
        String sql = "SELECT * FROM quiz_results WHERE user_id = ? AND quiz_id = ?";
        return jdbcTemplate.query(sql, resultRowMapper, userId, quizId).stream().findFirst();
    }

    public List<LeaderboardEntryDto> findLeaderboardByQuizId(Long quizId) {
        String sql = "SELECT qr.user_id, u.username, qr.score, qr.completed_at " +
                "FROM quiz_results qr " +
                "JOIN users u ON qr.user_id = u.id " +
                "WHERE qr.quiz_id = ? " +
                "ORDER BY qr.score DESC, qr.completed_at ASC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> LeaderboardEntryDto.builder()
                .userId(rs.getLong("user_id"))
                .username(rs.getString("username"))
                .score(rs.getInt("score"))
                .completedAt(rs.getTimestamp("completed_at").toLocalDateTime())
                .build(), quizId);
    }

    public Optional<UserStatsDto> getUserStats(Long userId) {
        String sql = "SELECT u.id as user_id, u.username, " +
                "COUNT(qr.id) as total_passed, " +
                "COALESCE(AVG(qr.score), 0.0) as avg_score, " +
                "COALESCE(SUM(qr.score), 0) as total_score " +
                "FROM users u " +
                "LEFT JOIN quiz_results qr ON u.id = qr.user_id " +
                "WHERE u.id = ? " +
                "GROUP BY u.id, u.username";

        return jdbcTemplate.query(sql, (rs, rowNum) -> UserStatsDto.builder()
                .userId(rs.getLong("user_id"))
                .username(rs.getString("username"))
                .totalQuizzesPassed(rs.getInt("total_passed"))
                .averageScore(rs.getDouble("avg_score"))
                .totalScore(rs.getInt("total_score"))
                .build(), userId).stream().findFirst();
    }
}