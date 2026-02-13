package com.batch.batch_app.mapper;

import com.batch.batch_app.domain.AiSummary;
import com.batch.batch_app.domain.FinancialDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface FinancialDataMapper {

    void upsertFinancialData(FinancialDataEntity entity);

    void batchUpsertFinancialData(@Param("list") List<FinancialDataEntity> entities);

    List<FinancialDataEntity> findByScrapedDate(@Param("scrapedDate") LocalDate scrapedDate);

    List<FinancialDataEntity> findBySourceAndDate(
            @Param("dataSource") String dataSource,
            @Param("scrapedDate") LocalDate scrapedDate);

    void insertAiSummary(AiSummary aiSummary);

    AiSummary findAiSummaryByDate(@Param("summaryDate") LocalDate summaryDate);
}
