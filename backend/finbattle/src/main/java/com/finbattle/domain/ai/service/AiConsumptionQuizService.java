package com.finbattle.domain.ai.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.finbattle.domain.ai.dto.AiConsumptionQuizDto;
import com.finbattle.domain.ai.dto.AiOptionDto;
import com.finbattle.domain.ai.model.AiMultipleChoiceQuiz;
import com.finbattle.domain.ai.model.AiOption;
import com.finbattle.domain.ai.model.AiQuiz;
import com.finbattle.domain.ai.repository.AiMultipleChoiceQuizRepository;
import com.finbattle.domain.ai.repository.AiOptionRepository;
import com.finbattle.domain.ai.repository.AiQuizRepository;
import com.finbattle.global.common.exception.exception.BusinessException;
import com.finbattle.global.common.model.dto.BaseResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiConsumptionQuizService {

    private final AiQuizRepository aiQuizRepository;
    private final AiMultipleChoiceQuizRepository multipleChoiceQuizRepository;
    private final AiOptionRepository aiOptionRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public List<Long> createConsumptionQuiz(Long memberId, Map<String, Long> consumptionMap) {
        String prompt = buildPrompt(consumptionMap);
        String gptAnswer = callOpenAi(prompt);
        List<ParsedQuiz> parsedList = parseGptAnswer(gptAnswer);
        List<Long> createdQuizIds = new ArrayList<>();
        for (ParsedQuiz parsed : parsedList) {
            AiQuiz aiQuiz = AiQuiz.builder()
                    .memberId(memberId)
                    .createdAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            AiQuiz savedQuiz = aiQuizRepository.save(aiQuiz);
            AiMultipleChoiceQuiz multiple = AiMultipleChoiceQuiz.builder()
                    .aiQuiz(savedQuiz)
                    .question(parsed.question())
                    .build();
            AiMultipleChoiceQuiz savedMultiple = multipleChoiceQuizRepository.save(multiple);
            for (int i = 0; i < parsed.options().size(); i++) {
                boolean isCorrect = (i == parsed.answerIndex());
                AiOption option = AiOption.builder()
                        .multipleChoiceQuiz(savedMultiple)
                        .optionText(parsed.options().get(i))
                        .isCorrect(isCorrect)
                        .build();
                aiOptionRepository.save(option);
            }
            createdQuizIds.add(savedQuiz.getAiQuizId());
        }
        return createdQuizIds;
    }

    private String buildPrompt(Map<String, Long> consumptionMap) {
        StringBuilder sb = new StringBuilder("사용자의 소비내역:\n");
        consumptionMap.forEach((category, amount) ->
                sb.append("- ").append(category).append(": ").append(amount).append("원\n")
        );
        long totalConsumption = consumptionMap.values().stream().mapToLong(Long::longValue).sum();
        sb.append("\n각 항목의 소비 비율:\n");
        consumptionMap.forEach((category, amount) -> {
            double ratio = totalConsumption > 0 ? (amount * 100.0 / totalConsumption) : 0;
            sb.append("- ").append(category)
                    .append(": ").append(String.format("%.1f", ratio)).append("%\n");
        });
        sb.append("\n");
        sb.append("""
            위 소비내역과 비율 정보를 참고하여, 다음 두 유형의 문제를 적절히 섞은 금융 관련 맞춤형 문제 10개를 생성해 주세요.
            소비내역 관련 문제랑, 관련 도메인 문제 반반 섞어서 내줘. 그리고 객관식이기 때문에 항상 정답이 존재해야해.
            
            [유형 1] 사용자의 소비 데이터에 직접 기반한 문제:
              - 예: "당신의 소비 내역에서 가장 큰 소비 항목은 무엇일까요?"
              - 예: "전체 소비에서 교육비가 차지하는 비율은 몇 %일까요?"
            
            [유형 2] 일반 금융 지식과 관련된 문제:
              - 예: "담보 대출 시 담보 물건의 가치가 중요한 이유는 무엇입니까?"
              - 예:  부동산 정책의 주요 목적은 무엇일까요?
            
            위 두 유형이 고루 섞여 총 10개의 서로 다른 문제를 만들어 주세요.
            각 문제는 4개의 보기와 정답(0~3 중 인덱스)을 포함한 JSON 배열 형식으로 응답해 주세요.
            """);
        return sb.toString();
    }

    private String callOpenAi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);
        Map<String, Object> message = Map.of("role", "user", "content", prompt);
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-3.5-turbo",
                "messages", List.of(message),
                "temperature", 0.7
        );
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, entity, Map.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new BusinessException(BaseResponseStatus.OPENAI_API_ERROR);
        }
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BusinessException(BaseResponseStatus.AI_RESPONSE_INVALID);
        }
        String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
        log.info("GPT 응답: {}", content);
        return content;
    }

    private List<ParsedQuiz> parseGptAnswer(String gptAnswer) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(gptAnswer, new com.fasterxml.jackson.core.type.TypeReference<List<ParsedQuiz>>() {});
        } catch (Exception e) {
            log.error("GPT 응답 파싱 실패: {}", e.getMessage());
            throw new BusinessException(BaseResponseStatus.AI_RESPONSE_INVALID);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ParsedQuiz(String question, List<String> options, int answerIndex) {}

    public List<AiConsumptionQuizDto> getLatestConsumptionQuizzes(Long memberId) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        List<AiQuiz> aiQuizzes = aiQuizRepository.findByMemberIdAndIsDeletedFalse(memberId, pageable);
        return aiQuizzes.stream().map(aiQuiz -> {
            AiMultipleChoiceQuiz multipleQuiz = multipleChoiceQuizRepository.findByAiQuiz(aiQuiz)
                    .orElseThrow(() -> new BusinessException(BaseResponseStatus.QUIZ_NOT_FOUND));
            List<AiOption> options = aiOptionRepository.findByMultipleChoiceQuiz(multipleQuiz);
            List<AiOptionDto> optionDtos = options.stream()
                    .map(opt -> new AiOptionDto(opt.getAiOptionId(), opt.getOptionText()))
                    .collect(Collectors.toList());
            return new AiConsumptionQuizDto(aiQuiz.getAiQuizId(), multipleQuiz.getQuestion(), optionDtos);
        }).collect(Collectors.toList());
    }
}
