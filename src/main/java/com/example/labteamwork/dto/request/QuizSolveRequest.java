package com.example.labteamwork.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class QuizSolveRequest {

    @NotEmpty(message = "Список ответов не может быть пустым")
    @Valid
    private List<QuestionAnswerRequest> answers;
}