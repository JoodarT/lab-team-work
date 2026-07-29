package com.example.labteamwork.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuizCreateRequest {

    @NotBlank(message = "Название викторины не должно быть пустым")
    @Size(max = 255, message = "Название не должно превышать 255 символов")
    private String title;

    private String description;

    private String category;

    @Min(value = 1, message = "Ограничение по времени должно быть больше 0 секунд")
    private Integer timeLimitSeconds;

    @NotEmpty(message = "Викторина должна содержать хотя бы один вопрос")
    @Valid
    private List<QuestionCreateRequest> questions;
}