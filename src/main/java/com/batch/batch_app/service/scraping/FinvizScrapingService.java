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
public class FinvizScrapingService {

    private final SeleniumClientService seleniumClientService;

    @Value("${scraping.finviz.base-url}")
    private String baseUrl;

    @Value("${scraping.finviz.screener-url}")
    private String screenerUrl;

    @Value("${scraping.finviz.news-url}")
    private String newsUrl;

    public List<ScrapedFinancialData> scrapeAll() {
        List<ScrapedFinancialData> allData = new ArrayList<>();

        allData.addAll(scrapeScreener());
        allData.addAll(scrapeNews());

        log.info("Finviz 총 {} 건 스크래핑 완료", allData.size());
        return allData;
    }

    public List<ScrapedFinancialData> scrapeScreener() {
        log.info("Finviz 스크리너 스크래핑 시작: {}", screenerUrl);
        try {
            // Top Gainers 스크래핑
            String gainersUrl = screenerUrl + "?s=ta_topgainers&f=cap_largeover";
            Document doc = seleniumClientService.fetchDocument(gainersUrl, baseUrl);
            return parseScreenerTable(doc);
        } catch (IOException e) {
            log.error("Finviz 스크리너 스크래핑 실패: {}", e.getMessage());
            return List.of();
        }
    }

    public List<ScrapedFinancialData> scrapeNews() {
        log.info("Finviz 뉴스 스크래핑 시작: {}", newsUrl);
        try {
            Document doc = seleniumClientService.fetchDocument(newsUrl, baseUrl);
            return parseNewsTable(doc);
        } catch (IOException e) {
            log.error("Finviz 뉴스 스크래핑 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private List<ScrapedFinancialData> parseScreenerTable(Document doc) {
        List<ScrapedFinancialData> dataList = new ArrayList<>();

        // finviz 스크리너 테이블 파싱 (JS 렌더링 후 DOM)
        Elements rows = doc.select("table.screener_table tr, table[data-testid='screener-table'] tr, #screener-content table tr");
        if (rows.isEmpty()) {
            rows = doc.select("table.table-light tr, table.styled-table-new tr, table:has(td a.screener-link-primary) tr");
        }

        boolean headerSkipped = false;
        for (Element row : rows) {
            Elements cells = row.select("td");
            if (cells.size() < 8) {
                headerSkipped = true;
                continue;
            }
            if (!headerSkipped) {
                headerSkipped = true;
                continue;
            }

            String ticker = cells.size() > 1 ? cells.get(1).text().trim() : "";
            String company = cells.size() > 2 ? cells.get(2).text().trim() : "";
            String price = cells.size() > 8 ? cells.get(8).text().trim() : "";
            String change = cells.size() > 9 ? cells.get(9).text().trim() : "";
            String volume = cells.size() > 10 ? cells.get(10).text().trim() : "";

            if (ticker.isEmpty()) continue;

            String itemName = ticker + " - " + company;

            dataList.add(ScrapedFinancialData.builder()
                    .dataSource(DataSource.FINVIZ)
                    .dataCategory(DataCategory.SCREENER)
                    .marketRegion(MarketRegion.US)
                    .itemName(itemName)
                    .currentValue(price)
                    .changePercent(change)
                    .extraInfo("거래량:" + volume)
                    .build());
        }

        log.info("Finviz 스크리너 {} 건 파싱 완료", dataList.size());
        return dataList;
    }

    private List<ScrapedFinancialData> parseNewsTable(Document doc) {
        List<ScrapedFinancialData> dataList = new ArrayList<>();

        // finviz 뉴스 테이블 파싱 (JS 렌더링 후 DOM)
        Elements newsRows = doc.select("table.news-table tr, table.t-home-table tr, #news table tr");
        if (newsRows.isEmpty()) {
            newsRows = doc.select("table.styled-table-new tr:has(a), div.news a, div.content a[href*='news']");
        }

        int count = 0;
        for (Element row : newsRows) {
            if (count >= 30) break;

            Element link = row.selectFirst("a.tab-link, a.tab-link-news, a.nn-tab-link, td a[href]");
            if (link == null) continue;

            String headline = link.text().trim();
            String source = row.select("span.news-link-right, td:last-child span").text().trim();
            String time = row.select("td:first-child").text().trim();

            if (headline.isEmpty()) continue;

            dataList.add(ScrapedFinancialData.builder()
                    .dataSource(DataSource.FINVIZ)
                    .dataCategory(DataCategory.NEWS)
                    .marketRegion(MarketRegion.US)
                    .itemName(headline)
                    .extraInfo(String.format("출처:%s, 시간:%s", source, time))
                    .build());

            count++;
        }

        log.info("Finviz 뉴스 {} 건 파싱 완료", dataList.size());
        return dataList;
    }
}
