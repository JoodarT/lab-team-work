package com.example.labteamwork.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OptionCreateRequest {

    @NotBlank(message = "Текст варианта ответа не должен быть пустым")
    private String optionText;

    @NotNull(message = "Укажите, является ли вариант ответа правильным (isCorrect)")
    private Boolean isCorrect;
}