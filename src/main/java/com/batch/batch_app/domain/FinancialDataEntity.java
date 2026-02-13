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
public class FinancialDataEntity {
    private Long id;
    private String dataSource;
    private String dataCategory;
    private String marketRegion;
    private String itemName;
    private String currentValue;
    private String changeValue;
    private String changePercent;
    private String extraInfo;
    private LocalDate scrapedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
