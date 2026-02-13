package com.batch.batch_app.service.notification;

import com.batch.batch_app.domain.KakaoToken;
import com.batch.batch_app.mapper.KakaoTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoTokenService {

    private final WebClient webClient;
    private final KakaoTokenMapper kakaoTokenMapper;

    @Value("${kakao.oauth.client-id:}")
    private String clientId;

    @Value("${kakao.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.oauth.token-url}")
    private String tokenUrl;

    public Optional<String> getValidAccessToken() {
        Optional<KakaoToken> tokenOpt = kakaoTokenMapper.findLatestToken();

        if (tokenOpt.isEmpty()) {
            log.warn("저장된 카카오 토큰 없음. 먼저 인증을 완료하세요.");
            return Optional.empty();
        }

        KakaoToken token = tokenOpt.get();

        // access_token이 만료되지 않은 경우
        if (token.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(5))) {
            return Optional.of(token.getAccessToken());
        }

        // refresh_token으로 갱신 시도
        if (token.getRefreshExpiresAt().isAfter(LocalDateTime.now())) {
            log.info("카카오 액세스 토큰 갱신 시도");
            return refreshToken(token);
        }

        log.warn("카카오 리프레시 토큰도 만료됨. 재인증 필요.");
        return Optional.empty();
    }

    public void saveTokenFromAuthCode(String authCode) {
        log.info("카카오 인증 코드로 토큰 발급 시작");

        Map<?, ?> response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "authorization_code")
                        .with("client_id", clientId)
                        .with("redirect_uri", redirectUri)
                        .with("code", authCode))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (response != null) {
            saveTokenResponse(response, null);
            log.info("카카오 토큰 발급 및 저장 완료");
        }
    }

    private Optional<String> refreshToken(KakaoToken existingToken) {
        try {
            Map<?, ?> response = webClient.post()
                    .uri(tokenUrl)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                            .with("client_id", clientId)
                            .with("refresh_token", existingToken.getRefreshToken()))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                saveTokenResponse(response, existingToken);
                return Optional.of((String) response.get("access_token"));
            }
        } catch (Exception e) {
            log.error("카카오 토큰 갱신 실패: {}", e.getMessage());
        }

        return Optional.empty();
    }

    private void saveTokenResponse(Map<?, ?> response, KakaoToken existingToken) {
        String accessToken = (String) response.get("access_token");
        String refreshToken = response.containsKey("refresh_token")
                ? (String) response.get("refresh_token")
                : (existingToken != null ? existingToken.getRefreshToken() : null);
        int expiresIn = ((Number) response.get("expires_in")).intValue();
        int refreshExpiresIn = response.containsKey("refresh_token_expires_in")
                ? ((Number) response.get("refresh_token_expires_in")).intValue()
                : 5184000; // 기본 60일

        LocalDateTime now = LocalDateTime.now();

        KakaoToken token = KakaoToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("bearer")
                .expiresAt(now.plusSeconds(expiresIn))
                .refreshExpiresAt(now.plusSeconds(refreshExpiresIn))
                .build();

        if (existingToken != null) {
            token.setId(existingToken.getId());
            kakaoTokenMapper.updateToken(token);
        } else {
            kakaoTokenMapper.insertToken(token);
        }
    }
}
