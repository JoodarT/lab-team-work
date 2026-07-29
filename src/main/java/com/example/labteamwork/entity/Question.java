package com.example.labteamwork.entity;

import com.example.labteamwork.entity.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Question {
    private Long id;
    private Long quizId;
    private String questionText;
    private List<Option> options;
}