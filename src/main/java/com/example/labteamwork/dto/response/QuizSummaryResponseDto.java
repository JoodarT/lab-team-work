package com.example.labteamwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizSummaryResponseDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer timeLimitSeconds;
    private Integer questionsCount;
    private String creatorUsername;
}