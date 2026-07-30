package com.example.labteamwork.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailResultDto {
    private Long id;
    private String questionText;
    private List<OptionDetailResponseDto> options;
}