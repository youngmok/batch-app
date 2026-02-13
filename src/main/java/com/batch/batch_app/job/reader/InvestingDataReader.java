package com.batch.batch_app.job.reader;

import com.batch.batch_app.domain.ScrapedFinancialData;
import com.batch.batch_app.service.scraping.InvestingScrapingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemReader;

import java.util.Iterator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class InvestingDataReader implements ItemReader<ScrapedFinancialData> {

    private final InvestingScrapingService investingScrapingService;
    private Iterator<ScrapedFinancialData> dataIterator;
    private boolean initialized = false;

    @Override
    public ScrapedFinancialData read() {
        if (!initialized) {
            log.info("Investing.com 데이터 스크래핑 시작");
            List<ScrapedFinancialData> data = investingScrapingService.scrapeAll();
            dataIterator = data.iterator();
            initialized = true;
            log.info("Investing.com 데이터 {} 건 로드 완료", data.size());
        }

        if (dataIterator != null && dataIterator.hasNext()) {
            return dataIterator.next();
        }
        return null;
    }
}
