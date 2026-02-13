package com.batch.batch_app.service.scraping;

import com.batch.batch_app.domain.ScrapedFinancialData;
import com.batch.batch_app.domain.enums.DataCategory;
import com.batch.batch_app.domain.enums.DataSource;
import com.batch.batch_app.domain.enums.MarketRegion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestingScrapingService {

    private final HttpClientService httpClientService;

    @Value("${scraping.investing.base-url}")
    private String baseUrl;

    @Value("${scraping.investing.indices-url}")
    private String indicesUrl;

    @Value("${scraping.investing.currencies-url}")
    private String currenciesUrl;

    @Value("${scraping.investing.commodities-url}")
    private String commoditiesUrl;

    @Value("${scraping.investing.crypto-url}")
    private String cryptoUrl;

    @Value("${scraping.investing.economic-calendar-url}")
    private String economicCalendarUrl;

    public List<ScrapedFinancialData> scrapeAll() {
        List<ScrapedFinancialData> allData = new ArrayList<>();

        allData.addAll(scrapeIndices());
        allData.addAll(scrapeCurrencies());
        allData.addAll(scrapeCommodities());
        allData.addAll(scrapeCrypto());
        allData.addAll(scrapeEconomicCalendar());

        log.info("Investing.com 총 {} 건 스크래핑 완료", allData.size());
        return allData;
    }

    public List<ScrapedFinancialData> scrapeIndices() {
        log.info("주요 지수 스크래핑 시작: {}", indicesUrl);
        try {
            Document doc = httpClientService.fetchDocument(indicesUrl, baseUrl);
            return parseMarketTable(doc, DataCategory.INDEX, MarketRegion.GLOBAL);
        } catch (IOException e) {
            log.error("지수 스크래핑 실패: {}", e.getMessage());
            return List.of();
        }
    }

    public List<ScrapedFinancialData> scrapeCurrencies() {
        log.info("환율 스크래핑 시작: {}", currenciesUrl);
        try {
            Document doc = httpClientService.fetchDocument(currenciesUrl, baseUrl);
            return parseMarketTable(doc, DataCategory.CURRENCY, MarketRegion.GLOBAL);
        } catch (IOException e) {
            log.error("환율 스크래핑 실패: {}", e.getMessage());
            return List.of();
        }
    }

    public List<ScrapedFinancialData> scrapeCommodities() {
        log.info("원자재 스크래핑 시작: {}", commoditiesUrl);
        try {
            Document doc = httpClientService.fetchDocument(commoditiesUrl, baseUrl);
            return parseMarketTable(doc, DataCategory.COMMODITY, MarketRegion.GLOBAL);
        } catch (IOException e) {
            log.error("원자재 스크래핑 실패: {}", e.getMessage());
            return List.of();
        }
    }

    public List<ScrapedFinancialData> scrapeCrypto() {
        log.info("암호화폐 스크래핑 시작: {}", cryptoUrl);
        try {
            Document doc = httpClientService.fetchDocument(cryptoUrl, baseUrl);
            return parseMarketTable(doc, DataCategory.CRYPTO, MarketRegion.GLOBAL);
        } catch (IOException e) {
            log.error("암호화폐 스크래핑 실패: {}", e.getMessage());
            return List.of();
        }
    }

    public List<ScrapedFinancialData> scrapeEconomicCalendar() {
        log.info("경제 캘린더 스크래핑 시작: {}", economicCalendarUrl);
        try {
            Document doc = httpClientService.fetchDocument(economicCalendarUrl, baseUrl);
            return parseEconomicCalendar(doc);
        } catch (IOException e) {
            log.error("경제 캘린더 스크래핑 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ScrapedFinancialData> parseMarketTable(Document doc, DataCategory category, MarketRegion region) {
        List<ScrapedFinancialData> dataList = new ArrayList<>();

        // investing.com 테이블 파싱 - 다양한 셀렉터 시도
        Elements tables = doc.select("table.genTbl, table.crossRatesTbl, table[data-test='dynamic-table'], table.common-table");
        if (tables.isEmpty()) {
            tables = doc.select("table");
        }

        for (Element table : tables) {
            Elements rows = table.select("tbody tr");
            for (Element row : rows) {
                Elements cells = row.select("td");
                if (cells.size() < 3) continue;

                String itemName = extractText(cells, 1, 0);
                if (itemName.isEmpty()) continue;

                String currentValue = extractText(cells, 2, 1);
                String changeValue = cells.size() > 4 ? extractText(cells, 5, 4) : "";
                String changePercent = cells.size() > 5 ? extractText(cells, 6, 5) : "";

                dataList.add(ScrapedFinancialData.builder()
                        .dataSource(DataSource.INVESTING)
                        .dataCategory(category)
                        .marketRegion(region)
                        .itemName(itemName.trim())
                        .currentValue(currentValue.trim())
                        .changeValue(changeValue.trim())
                        .changePercent(changePercent.trim())
                        .build());
            }
        }

        log.info("{} 카테고리 {} 건 파싱 완료", category, dataList.size());
        return dataList;
    }

    private List<ScrapedFinancialData> parseEconomicCalendar(Document doc) {
        List<ScrapedFinancialData> dataList = new ArrayList<>();

        Elements rows = doc.select("tr.js-event-item, tr[data-test='economic-calendar-row']");
        if (rows.isEmpty()) {
            rows = doc.select("table#economicCalendarData tbody tr");
        }

        for (Element row : rows) {
            String time = row.select("td.time, td:nth-child(1)").text().trim();
            String country = row.select("td.flagCur span, td:nth-child(2)").text().trim();
            String event = row.select("td.event a, td:nth-child(4)").text().trim();
            String actual = row.select("td.act, td[data-test='actual']").text().trim();
            String forecast = row.select("td.fore, td[data-test='forecast']").text().trim();
            String previous = row.select("td.prev, td[data-test='previous']").text().trim();

            if (event.isEmpty()) continue;

            String extraInfo = String.format("시간:%s, 국가:%s, 예상:%s, 이전:%s", time, country, forecast, previous);

            dataList.add(ScrapedFinancialData.builder()
                    .dataSource(DataSource.INVESTING)
                    .dataCategory(DataCategory.ECONOMIC_CALENDAR)
                    .marketRegion(MarketRegion.GLOBAL)
                    .itemName(event)
                    .currentValue(actual)
                    .extraInfo(extraInfo)
                    .build());
        }

        log.info("경제 캘린더 {} 건 파싱 완료", dataList.size());
        return dataList;
    }

    private String extractText(Elements cells, int primaryIndex, int fallbackIndex) {
        if (primaryIndex < cells.size()) {
            String text = cells.get(primaryIndex).text();
            if (!text.isEmpty()) return text;
        }
        if (fallbackIndex < cells.size()) {
            return cells.get(fallbackIndex).text();
        }
        return "";
    }
}
