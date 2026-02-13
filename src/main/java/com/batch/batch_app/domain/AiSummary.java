package com.batch.batch_app.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSummary {
    private Long id;
    private LocalDate summaryDate;
    private String summaryText;
    private String modelUsed;
    private Integer tokenUsed;
    private LocalDateTime createdAt;
}
