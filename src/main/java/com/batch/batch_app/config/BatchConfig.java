package com.batch.batch_app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchConfig {

    private final JobOperator jobOperator;
    private final Job financialDataJob;

    @Scheduled(cron = "${scheduler.cron:0 0 7 * * *}")
    public void runFinancialDataJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("date", LocalDate.now().toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            log.info("스케줄러에 의한 Job 실행 시작: {}", params);
            jobOperator.start(financialDataJob, params);

        } catch (Exception e) {
            log.error("스케줄러 Job 실행 실패: {}", e.getMessage(), e);
        }
    }
}
