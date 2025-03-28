package com.finbattle.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConsumptionQuizAnswerDto {
    private Long quizId;
    private int selectedIndex;
}
