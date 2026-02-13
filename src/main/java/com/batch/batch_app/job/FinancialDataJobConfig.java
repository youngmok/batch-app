package com.batch.batch_app.job;

import com.batch.batch_app.job.listener.JobLoggingListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FinancialDataJobConfig {

    private final JobRepository jobRepository;
    private final Step investingScrapingStep;
    private final Step finvizScrapingStep;
    private final Step summaryNotificationStep;
    private final JobLoggingListener jobLoggingListener;

    @Bean
    public Job financialDataJob() {
        return new JobBuilder("financialDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobLoggingListener)
                .start(investingScrapingStep)
                .next(finvizScrapingStep)
                .next(summaryNotificationStep)
                .build();
    }
}
