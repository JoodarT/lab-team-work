package com.example.labteamwork.dao;

import com.example.labteamwork.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class QuestionDao {

    private final JdbcTemplate jdbcTemplate;
    private final OptionDao optionDao;

    private final RowMapper<Question> questionRowMapper = (rs, rowNum) -> Question.builder()
            .id(rs.getLong("id"))
            .quizId(rs.getLong("quiz_id"))
            .questionText(rs.getString("question_text"))
            .build();

    public Question save(Question question) {
        String sql = "INSERT INTO questions (quiz_id, question_text) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setLong(1, question.getQuizId());
            ps.setString(2, question.getQuestionText());
            return ps;
        }, keyHolder);

        question.setId(keyHolder.getKey().longValue());

        if (question.getOptions() != null) {
            question.getOptions().forEach(option -> {
                option.setQuestionId(question.getId());
                optionDao.save(option);
            });
        }

        return question;
    }

    public List<Question> findByQuizId(Long quizId) {
        String sql = "SELECT * FROM questions WHERE quiz_id = ?";
        List<Question> questions = jdbcTemplate.query(sql, questionRowMapper, quizId);

        questions.forEach(q -> q.setOptions(optionDao.findByQuestionId(q.getId())));
        return questions;
    }

    public int countByQuizId(Long quizId) {
        String sql = "SELECT COUNT(*) FROM questions WHERE quiz_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, quizId);
        return count != null ? count : 0;
    }
}