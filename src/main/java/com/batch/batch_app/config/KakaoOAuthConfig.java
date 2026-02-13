package com.batch.batch_app.config;

import com.batch.batch_app.service.notification.KakaoTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class KakaoOAuthConfig {

    private final KakaoTokenService kakaoTokenService;

    @Value("${kakao.oauth.client-id:}")
    private String clientId;

    @Value("${kakao.oauth.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.oauth.auth-url}")
    private String authUrl;

    @GetMapping("/oauth/kakao")
    public ResponseEntity<String> kakaoAuth() {
        String url = String.format("%s?client_id=%s&redirect_uri=%s&response_type=code&scope=talk_message",
                authUrl, clientId, redirectUri);
        return ResponseEntity.ok("카카오 인증 URL: " + url);
    }

    @GetMapping("/oauth/kakao/callback")
    public ResponseEntity<String> kakaoCallback(@RequestParam("code") String code) {
        log.info("카카오 인증 콜백 수신");
        kakaoTokenService.saveTokenFromAuthCode(code);
        return ResponseEntity.ok("카카오 인증 완료! 토큰이 저장되었습니다.");
    }
}
