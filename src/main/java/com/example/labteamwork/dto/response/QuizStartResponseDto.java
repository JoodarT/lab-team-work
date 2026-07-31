package com.example.labteamwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizStartResponseDto {
    private Long quizId;
    private LocalDateTime startedAt;
    private Integer timeLimitSeconds;
    private String message;
}