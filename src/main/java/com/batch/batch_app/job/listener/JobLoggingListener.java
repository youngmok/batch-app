package com.batch.batch_app.job.listener;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class JobLoggingListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        MDC.put("jobId", String.valueOf(jobExecution.getId()));
        MDC.put("jobName", jobExecution.getJobInstance().getJobName());

        log.info("====================================");
        log.info("Job 시작: {} (ID: {})",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getId());
        log.info("Job 파라미터: {}", jobExecution.getJobParameters());
        log.info("====================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        Duration duration = Duration.between(
                jobExecution.getStartTime(),
                jobExecution.getEndTime() != null ? jobExecution.getEndTime() : java.time.LocalDateTime.now());

        log.info("====================================");
        log.info("Job 완료: {} (ID: {})",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getId());
        log.info("Job 상태: {}", jobExecution.getStatus());
        log.info("소요 시간: {}초", duration.getSeconds());

        if (!jobExecution.getAllFailureExceptions().isEmpty()) {
            log.error("실패 원인:");
            jobExecution.getAllFailureExceptions()
                    .forEach(e -> log.error("  - {}", e.getMessage()));
        }

        log.info("====================================");

        MDC.remove("jobId");
        MDC.remove("jobName");
    }
}
