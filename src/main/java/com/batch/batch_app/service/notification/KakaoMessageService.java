package com.batch.batch_app.service.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoMessageService {

    private final WebClient webClient;
    private final KakaoTokenService kakaoTokenService;
    private final ObjectMapper objectMapper;

    @Value("${kakao.api.send-me-url}")
    private String sendMeUrl;

    public boolean sendToMe(String title, String message) {
        Optional<String> tokenOpt = kakaoTokenService.getValidAccessToken();

        if (tokenOpt.isEmpty()) {
            log.error("카카오톡 발송 실패: 유효한 액세스 토큰 없음");
            return false;
        }

        String accessToken = tokenOpt.get();

        try {
            // 메시지가 너무 길면 분할
            if (message.length() > 1500) {
                message = message.substring(0, 1497) + "...";
            }

            String templateObject = buildTextTemplate(title, message);

            String response = webClient.post()
                    .uri(sendMeUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("template_object", templateObject))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("카카오톡 발송 성공: {}", response);
            return true;

        } catch (Exception e) {
            log.error("카카오톡 발송 실패: {}", e.getMessage());
            return false;
        }
    }

    private String buildTextTemplate(String title, String message) throws JsonProcessingException {
        Map<String, Object> template = Map.of(
                "object_type", "text",
                "text", String.format("[%s]\n\n%s", title, message),
                "link", Map.of(
                        "web_url", "https://kr.investing.com",
                        "mobile_web_url", "https://kr.investing.com"
                ),
                "button_title", "상세보기"
        );

        return objectMapper.writeValueAsString(template);
    }
}
