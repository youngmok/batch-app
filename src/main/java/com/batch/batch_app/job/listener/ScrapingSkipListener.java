package com.batch.batch_app.job.listener;

import com.batch.batch_app.domain.FinancialDataEntity;
import com.batch.batch_app.domain.ScrapedFinancialData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScrapingSkipListener implements SkipListener<ScrapedFinancialData, FinancialDataEntity> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("읽기 중 스킵 발생: {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(ScrapedFinancialData item, Throwable t) {
        log.warn("처리 중 스킵 발생 - 항목: {} | 원인: {}",
                item != null ? item.getItemName() : "null",
                t.getMessage());
    }

    @Override
    public void onSkipInWrite(FinancialDataEntity item, Throwable t) {
        log.warn("쓰기 중 스킵 발생 - 항목: {} | 원인: {}",
                item != null ? item.getItemName() : "null",
                t.getMessage());
    }
}
