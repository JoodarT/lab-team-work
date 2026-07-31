package com.example.labteamwork.dao;

import com.example.labteamwork.dto.response.GlobalLeaderboardEntryDto;
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
import java.util.Objects;
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
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, result.getUserId());
            ps.setLong(2, result.getQuizId());
            ps.setInt(3, result.getScore());
            ps.setTimestamp(4, Timestamp.valueOf(result.getCompletedAt()));
            return ps;
        }, keyHolder);

        result.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
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
        String sql = "SELECT u.id as user_id, u.username, u.email, " +
                "COUNT(DISTINCT qr.id) as total_quizzes_taken, " +
                "(SELECT COUNT(*) FROM quizzes q WHERE q.creator_id = u.id) as total_quizzes_created, " +
                "COALESCE(SUM(qr.score), 0) as total_score, " +
                "COALESCE(AVG(CASE WHEN q_counts.total_q > 0 THEN (qr.score * 100.0 / q_counts.total_q) ELSE 0 END), 0.0) as average_percentage " +
                "FROM users u " +
                "LEFT JOIN quiz_results qr ON u.id = qr.user_id " +
                "LEFT JOIN (SELECT quiz_id, COUNT(*) as total_q FROM questions GROUP BY quiz_id) q_counts ON qr.quiz_id = q_counts.quiz_id " +
                "WHERE u.id = ? " +
                "GROUP BY u.id, u.username, u.email";

        return jdbcTemplate.query(sql, (rs, rowNum) -> UserStatsDto.builder()
                .userId(rs.getLong("user_id"))
                .username(rs.getString("username"))
                .email(rs.getString("email"))
                .totalQuizzesTaken(rs.getInt("total_quizzes_taken"))
                .totalQuizzesCreated(rs.getInt("total_quizzes_created"))
                .totalScore(rs.getInt("total_score"))
                .averagePercentage(Math.round(rs.getDouble("average_percentage") * 100.0) / 100.0)
                .build(), userId).stream().findFirst();
    }

    public List<GlobalLeaderboardEntryDto> getGlobalLeaderboard(int limit) {
        String sql = "SELECT u.id as user_id, u.username, SUM(qr.score) as total_score " +
                "FROM users u " +
                "JOIN quiz_results qr ON u.id = qr.user_id " +
                "GROUP BY u.id, u.username " +
                "ORDER BY total_score DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> GlobalLeaderboardEntryDto.builder()
                .userId(rs.getLong("user_id"))
                .username(rs.getString("username"))
                .totalScore(rs.getInt("total_score"))
                .build(), limit);
    }
}