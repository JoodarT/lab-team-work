package com.example.labteamwork.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResult {
    private Long id;
    private Long userId;
    private Long quizId;
    private Integer score;
    private LocalDateTime completedAt;
}