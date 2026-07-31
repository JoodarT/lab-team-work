package com.example.labteamwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GlobalLeaderboardEntryDto {
    private Long userId;
    private String username;
    private Integer totalScore;
}