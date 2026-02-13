package com.batch.batch_app.job.step;

import com.batch.batch_app.domain.FinancialDataEntity;
import com.batch.batch_app.domain.ScrapedFinancialData;
import com.batch.batch_app.job.listener.ScrapingSkipListener;
import com.batch.batch_app.job.listener.StepLoggingListener;
import com.batch.batch_app.job.processor.InvestingDataProcessor;
import com.batch.batch_app.job.reader.InvestingDataReader;
import com.batch.batch_app.job.writer.FinancialDataWriter;
import com.batch.batch_app.mapper.FinancialDataMapper;
import com.batch.batch_app.service.scraping.InvestingScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class InvestingScrapingStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InvestingScrapingService investingScrapingService;
    private final FinancialDataMapper financialDataMapper;
    private final StepLoggingListener stepLoggingListener;
    private final ScrapingSkipListener scrapingSkipListener;

    @Bean
    public Step investingScrapingStep() {
        return new StepBuilder("investingScrapingStep", jobRepository)
                .<ScrapedFinancialData, FinancialDataEntity>chunk(100)
                .transactionManager(transactionManager)
                .reader(investingDataReader())
                .processor(investingDataProcessor())
                .writer(financialDataWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(50)
                .retry(Exception.class)
                .retryLimit(3)
                .listener(stepLoggingListener)
                .skipListener(scrapingSkipListener)
                .build();
    }

    @Bean
    public InvestingDataReader investingDataReader() {
        return new InvestingDataReader(investingScrapingService);
    }

    @Bean
    public InvestingDataProcessor investingDataProcessor() {
        return new InvestingDataProcessor();
    }

    @Bean
    public FinancialDataWriter financialDataWriter() {
        return new FinancialDataWriter(financialDataMapper);
    }
}
