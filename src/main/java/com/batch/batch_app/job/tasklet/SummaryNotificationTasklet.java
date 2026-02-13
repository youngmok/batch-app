package com.batch.batch_app.job.tasklet;

import com.batch.batch_app.domain.AiSummary;
import com.batch.batch_app.service.ai.AiSummaryService;
import com.batch.batch_app.service.notification.KakaoMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@RequiredArgsConstructor
public class SummaryNotificationTasklet implements Tasklet {

    private final AiSummaryService aiSummaryService;
    private final KakaoMessageService kakaoMessageService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        LocalDate today = LocalDate.now();
        log.info("AI 요약 및 카카오톡 발송 시작: {}", today);

        // 1. AI 요약 생성
        AiSummary summary = aiSummaryService.generateDailySummary(today);

        // 2. 카카오톡 발송
        String title = String.format("📊 일일 시장 브리핑 (%s)",
                today.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));

        boolean sent = kakaoMessageService.sendToMe(title, summary.getSummaryText());

        if (sent) {
            log.info("카카오톡 발송 성공");
        } else {
            log.warn("카카오톡 발송 실패 - 요약은 DB에 저장됨");
        }

        return RepeatStatus.FINISHED;
    }
}
