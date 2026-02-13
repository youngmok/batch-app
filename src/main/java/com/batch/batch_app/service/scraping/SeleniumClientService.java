package com.batch.batch_app.service.scraping;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class SeleniumClientService {

    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36"
    );

    private final Random random = new Random();

    @Value("${scraping.delay-ms:2000}")
    private int delayMs;

    @Value("${scraping.max-retry:3}")
    private int maxRetry;

    @Value("${scraping.selenium.wait-seconds:15}")
    private int waitSeconds;

    public Document fetchDocument(String url) throws IOException {
        return fetchDocument(url, null);
    }

    public Document fetchDocument(String url, String referrer) throws IOException {
        return fetchDocument(url, referrer, "table, div[data-test], #__next");
    }

    public Document fetchDocument(String url, String referrer, String waitForSelector) throws IOException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            WebDriver driver = null;
            try {
                if (attempt > 1) {
                    Thread.sleep(delayMs * attempt);
                }

                driver = createDriver();

                log.debug("페이지 요청 시작: {} (시도 {}회)", url, attempt);
                driver.get(url);

                // JS 렌더링 대기
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(waitSeconds));
                wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(waitForSelector)));

                // 추가 렌더링 대기
                Thread.sleep(delayMs);

                String pageSource = driver.getPageSource();
                Document doc = Jsoup.parse(pageSource, url);

                log.debug("페이지 로드 성공: {} (시도 {}회)", url, attempt);
                return doc;

            } catch (Exception e) {
                lastException = new IOException("페이지 로드 실패: " + url, e);
                log.warn("페이지 로드 실패 (시도 {}/{}): {} - {}", attempt, maxRetry, url, e.getMessage());
            } finally {
                quitDriver(driver);
            }
        }

        throw new IOException("최대 재시도 횟수 초과: " + url, lastException);
    }

    private WebDriver createDriver() {
        String userAgent = USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=" + userAgent);
        options.addArguments("--lang=ko-KR");
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);

        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(15));
        return driver;
    }

    private void quitDriver(WebDriver driver) {
        if (driver != null) {
            try {
                driver.quit();
            } catch (Exception e) {
                log.warn("WebDriver 종료 실패: {}", e.getMessage());
            }
        }
    }
}
