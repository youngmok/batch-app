package com.batch.batch_app.job.step;

import com.batch.batch_app.domain.FinancialDataEntity;
import com.batch.batch_app.domain.ScrapedFinancialData;
import com.batch.batch_app.job.listener.ScrapingSkipListener;
import com.batch.batch_app.job.listener.StepLoggingListener;
import com.batch.batch_app.job.processor.FinvizDataProcessor;
import com.batch.batch_app.job.reader.FinvizDataReader;
import com.batch.batch_app.job.writer.FinancialDataWriter;
import com.batch.batch_app.mapper.FinancialDataMapper;
import com.batch.batch_app.service.scraping.FinvizScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class FinvizScrapingStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FinvizScrapingService finvizScrapingService;
    private final FinancialDataMapper financialDataMapper;
    private final StepLoggingListener stepLoggingListener;
    private final ScrapingSkipListener scrapingSkipListener;

    @Bean
    public Step finvizScrapingStep() {
        return new StepBuilder("finvizScrapingStep", jobRepository)
                .<ScrapedFinancialData, FinancialDataEntity>chunk(100)
                .transactionManager(transactionManager)
                .reader(finvizDataReader())
                .processor(finvizDataProcessor())
                .writer(finvizFinancialDataWriter())
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
    public FinvizDataReader finvizDataReader() {
        return new FinvizDataReader(finvizScrapingService);
    }

    @Bean
    public FinvizDataProcessor finvizDataProcessor() {
        return new FinvizDataProcessor();
    }

    @Bean
    public FinancialDataWriter finvizFinancialDataWriter() {
        return new FinancialDataWriter(financialDataMapper);
    }
}
