package com.batch.batch_app.job.processor;

import com.batch.batch_app.domain.FinancialDataEntity;
import com.batch.batch_app.domain.ScrapedFinancialData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;

import java.time.LocalDate;

@Slf4j
public class FinvizDataProcessor implements ItemProcessor<ScrapedFinancialData, FinancialDataEntity> {

    @Override
    public FinancialDataEntity process(ScrapedFinancialData item) {
        if (item.getItemName() == null || item.getItemName().isBlank()) {
            log.debug("빈 항목 스킵");
            return null;
        }

        return FinancialDataEntity.builder()
                .dataSource(item.getDataSource().name())
                .dataCategory(item.getDataCategory().name())
                .marketRegion(item.getMarketRegion().name())
                .itemName(truncate(item.getItemName(), 200))
                .currentValue(truncate(item.getCurrentValue(), 50))
                .changeValue(truncate(item.getChangeValue(), 50))
                .changePercent(truncate(item.getChangePercent(), 20))
                .extraInfo(item.getExtraInfo())
                .scrapedDate(LocalDate.now())
                .build();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() > maxLength ? value.substring(0, maxLength) : value;
    }
}
