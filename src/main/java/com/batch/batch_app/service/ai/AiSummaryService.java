package com.batch.batch_app.service.ai;

import com.batch.batch_app.domain.AiSummary;
import com.batch.batch_app.domain.FinancialDataEntity;
import com.batch.batch_app.mapper.FinancialDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private final WebClient webClient;
    private final FinancialDataMapper financialDataMapper;

    @Value("${ai.openai.api-key:}")
    private String apiKey;

    @Value("${ai.openai.model:gpt-4o}")
    private String model;

    @Value("${ai.openai.api-url}")
    private String apiUrl;

    @Value("${ai.openai.max-tokens:2000}")
    private int maxTokens;

    public AiSummary generateDailySummary(LocalDate date) {
        log.info("AI 요약 생성 시작: {}", date);

        List<FinancialDataEntity> data = financialDataMapper.findByScrapedDate(date);
        if (data.isEmpty()) {
            log.warn("요약할 데이터 없음: {}", date);
            return AiSummary.builder()
                    .summaryDate(date)
                    .summaryText("오늘 수집된 금융 데이터가 없습니다.")
                    .modelUsed(model)
                    .build();
        }

        String prompt = buildPrompt(data, date);
        String summaryText = callOpenAiApi(prompt);

        AiSummary summary = AiSummary.builder()
                .summaryDate(date)
                .summaryText(summaryText)
                .modelUsed(model)
                .build();

        financialDataMapper.insertAiSummary(summary);
        log.info("AI 요약 생성 및 저장 완료: {}", date);

        return summary;
    }

    private String buildPrompt(List<FinancialDataEntity> data, LocalDate date) {
        Map<String, List<FinancialDataEntity>> grouped = data.stream()
                .collect(Collectors.groupingBy(FinancialDataEntity::getDataCategory));

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("오늘(%s) 수집된 금융 데이터를 바탕으로 한국어 시장 브리핑을 작성해주세요.\n\n", date));
        sb.append("다음 형식으로 작성해주세요:\n");
        sb.append("1. 📊 오늘의 시장 요약 (3-4줄)\n");
        sb.append("2. 📈 주요 지수 동향\n");
        sb.append("3. 💱 환율/원자재 동향\n");
        sb.append("4. 🪙 암호화폐 동향\n");
        sb.append("5. 📰 주요 뉴스 하이라이트\n");
        sb.append("6. 🔮 주목할 포인트\n\n");
        sb.append("간결하고 핵심적으로 작성하되, 카카오톡 메시지로 보내기 적합한 길이(1500자 이내)로 작성해주세요.\n\n");

        sb.append("=== 수집 데이터 ===\n\n");

        grouped.forEach((category, items) -> {
            sb.append(String.format("[%s]\n", category));
            for (FinancialDataEntity item : items) {
                sb.append(String.format("- %s: %s", item.getItemName(),
                        item.getCurrentValue() != null ? item.getCurrentValue() : "N/A"));
                if (item.getChangePercent() != null && !item.getChangePercent().isEmpty()) {
                    sb.append(String.format(" (%s)", item.getChangePercent()));
                }
                sb.append("\n");
            }
            sb.append("\n");
        });

        return sb.toString();
    }

    private String callOpenAiApi(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("OpenAI API 키가 설정되지 않음. 기본 요약 반환");
            return "AI 요약 서비스가 설정되지 않았습니다. OPENAI_API_KEY 환경변수를 설정해주세요.";
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", "당신은 금융 시장 분석 전문가입니다. 한국어로 간결하고 통찰력 있는 시장 브리핑을 작성합니다."),
                            Map.of("role", "user", "content", prompt)
                    ),
                    "max_tokens", maxTokens,
                    "temperature", 0.7
            );

            Map<?, ?> response = webClient.post()
                    .uri(apiUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                List<?> choices = (List<?>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
                    return (String) message.get("content");
                }
            }

            return "AI 요약 생성에 실패했습니다.";

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패: {}", e.getMessage());
            return "AI 요약 생성 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
