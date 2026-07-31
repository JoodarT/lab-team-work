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
public class QuizSession {
    private Long id;
    private Long userId;
    private Long quizId;
    private LocalDateTime startedAt;
}