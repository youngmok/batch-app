package com.batch.batch_app.domain;

import com.batch.batch_app.domain.enums.DataCategory;
import com.batch.batch_app.domain.enums.DataSource;
import com.batch.batch_app.domain.enums.MarketRegion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScrapedFinancialData {
    private DataSource dataSource;
    private DataCategory dataCategory;
    private MarketRegion marketRegion;
    private String itemName;
    private String currentValue;
    private String changeValue;
    private String changePercent;
    private String extraInfo;
}
