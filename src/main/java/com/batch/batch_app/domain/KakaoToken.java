package com.batch.batch_app.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoToken {
    private Long id;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private LocalDateTime expiresAt;
    private LocalDateTime refreshExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
