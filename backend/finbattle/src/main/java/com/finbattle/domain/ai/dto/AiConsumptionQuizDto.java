package com.finbattle.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConsumptionQuizDto {
    private Long quizId;
    private String question;
    private List<AiOptionDto> options;
}
