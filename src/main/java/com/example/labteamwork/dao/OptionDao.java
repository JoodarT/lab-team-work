package com.example.labteamwork.dao;

import com.example.labteamwork.entity.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OptionDao {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Option> optionRowMapper = (rs, rowNum) -> Option.builder()
            .id(rs.getLong("id"))
            .questionId(rs.getLong("question_id"))
            .optionText(rs.getString("option_text"))
            .isCorrect(rs.getBoolean("is_correct"))
            .build();

    public Option save(Option option) {
        String sql = "INSERT INTO options (question_id, option_text, is_correct) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, option.getQuestionId());
            ps.setString(2, option.getOptionText());
            ps.setBoolean(3, option.getIsCorrect());
            return ps;
        }, keyHolder);

        option.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return option;
    }

    public List<Option> findByQuestionId(Long questionId) {
        String sql = "SELECT * FROM options WHERE question_id = ?";
        return jdbcTemplate.query(sql, optionRowMapper, questionId);
    }

    public Optional<Option> findById(Long id) {
        String sql = "SELECT * FROM options WHERE id = ?";
        return jdbcTemplate.query(sql, optionRowMapper, id).stream().findFirst();
    }
}