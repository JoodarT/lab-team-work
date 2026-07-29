package com.example.labteamwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private Long userId;
    private String username;
    private Integer totalQuizzesPassed;
    private Double averageScore;
    private Integer totalScore;
}