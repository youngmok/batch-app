package com.batch.batch_app.service.scraping;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class HttpClientService {

    private static final List<String> USER_AGENTS = List.of(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:125.0) Gecko/20100101 Firefox/125.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.4 Safari/605.1.15",
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36"
    );

    private final Random random = new Random();

    @Value("${scraping.delay-ms:2000}")
    private int delayMs;

    @Value("${scraping.max-retry:3}")
    private int maxRetry;

    public Document fetchDocument(String url) throws IOException {
        return fetchDocument(url, null);
    }

    public Document fetchDocument(String url, String referrer) throws IOException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {
                if (attempt > 1) {
                    Thread.sleep(delayMs * attempt);
                }

                String userAgent = USER_AGENTS.get(random.nextInt(USER_AGENTS.size()));

                Connection connection = Jsoup.connect(url)
                        .userAgent(userAgent)
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .header("Upgrade-Insecure-Requests", "1")
                        .header("Cache-Control", "max-age=0")
                        .followRedirects(true)
                        .timeout(30000);

                if (referrer != null) {
                    connection.referrer(referrer);
                }

                Document doc = connection.get();
                log.debug("페이지 로드 성공: {} (시도 {}회)", url, attempt);

                Thread.sleep(delayMs);
                return doc;

            } catch (IOException e) {
                lastException = e;
                log.warn("페이지 로드 실패 (시도 {}/{}): {} - {}", attempt, maxRetry, url, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("요청이 인터럽트됨: " + url, e);
            }
        }

        throw new IOException("최대 재시도 횟수 초과: " + url, lastException);
    }
}
