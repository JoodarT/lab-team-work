package com.example.labteamwork.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer timeLimitSeconds;
    private Long creatorId;
    private List<Question> questions;
}