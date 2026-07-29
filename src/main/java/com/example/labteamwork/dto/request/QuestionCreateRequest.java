package com.example.labteamwork.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuestionCreateRequest {

    @NotBlank(message = "Текст вопроса не должен быть пустым")
    private String questionText;

    @NotEmpty(message = "У вопроса должен быть хотя бы один вариант ответа")
    @Size(min = 2, message = "Вопрос должен содержать как минимум 2 варианта ответа")
    @Valid
    private List<OptionCreateRequest> options;
}