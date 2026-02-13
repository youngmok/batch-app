package com.batch.batch_app.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
public class JobRunController {

    private final JobOperator jobOperator;
    private final Job financialDataJob;

    @PostMapping("/api/job/run")
    public ResponseEntity<String> runJob() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addString("date", LocalDate.now().toString())
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            log.info("수동 Job 실행 요청");
            jobOperator.start(financialDataJob, params);

            return ResponseEntity.ok("Job 실행 시작됨");
        } catch (Exception e) {
            log.error("Job 실행 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Job 실행 실패: " + e.getMessage());
        }
    }
}
