package com.batch.batch_app.job.step;

import com.batch.batch_app.job.listener.StepLoggingListener;
import com.batch.batch_app.job.tasklet.SummaryNotificationTasklet;
import com.batch.batch_app.service.ai.AiSummaryService;
import com.batch.batch_app.service.notification.KakaoMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class SummaryNotificationStepConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final AiSummaryService aiSummaryService;
    private final KakaoMessageService kakaoMessageService;
    private final StepLoggingListener stepLoggingListener;

    @Bean
    public Step summaryNotificationStep() {
        return new StepBuilder("summaryNotificationStep", jobRepository)
                .tasklet(summaryNotificationTasklet(), transactionManager)
                .listener(stepLoggingListener)
                .build();
    }

    @Bean
    public SummaryNotificationTasklet summaryNotificationTasklet() {
        return new SummaryNotificationTasklet(aiSummaryService, kakaoMessageService);
    }
}
