package com.example.labteamwork.dao;

import com.example.labteamwork.entity.QuizRating;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

@Repository
@RequiredArgsConstructor
public class QuizRatingDao {

    private final JdbcTemplate jdbcTemplate;

    public QuizRating save(QuizRating rating) {
        String sql = "INSERT INTO quiz_ratings (user_id, quiz_id, rating) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, rating.getUserId());
            ps.setLong(2, rating.getQuizId());
            ps.setInt(3, rating.getRating());
            return ps;
        }, keyHolder);

        rating.setId(keyHolder.getKey().longValue());
        return rating;
    }

    public boolean existsByUserIdAndQuizId(Long userId, Long quizId) {
        String sql = "SELECT COUNT(*) FROM quiz_ratings WHERE user_id = ? AND quiz_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, quizId);
        return count != null && count > 0;
    }
}