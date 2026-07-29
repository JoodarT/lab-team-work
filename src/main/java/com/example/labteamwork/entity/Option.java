package com.example.labteamwork.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Option {
    private Long id;
    private Long questionId;
    private String optionText;
    private Boolean isCorrect;
}