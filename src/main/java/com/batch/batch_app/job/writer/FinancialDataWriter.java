package com.batch.batch_app.job.writer;

import com.batch.batch_app.domain.FinancialDataEntity;
import com.batch.batch_app.mapper.FinancialDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

@Slf4j
@RequiredArgsConstructor
public class FinancialDataWriter implements ItemWriter<FinancialDataEntity> {

    private final FinancialDataMapper financialDataMapper;

    @Override
    public void write(Chunk<? extends FinancialDataEntity> chunk) {
        log.debug("DB 저장 시작: {} 건", chunk.size());

        for (FinancialDataEntity entity : chunk) {
            financialDataMapper.upsertFinancialData(entity);
        }

        log.debug("DB 저장 완료: {} 건", chunk.size());
    }
}
