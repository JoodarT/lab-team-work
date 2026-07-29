package com.example.labteamwork.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionAnswerRequest {

    @NotNull(message = "ID вопроса обязателен")
    private Long questionId;

    @NotNull(message = "ID выбранного варианта ответа обязателен")
    private Long selectedOptionId;
}