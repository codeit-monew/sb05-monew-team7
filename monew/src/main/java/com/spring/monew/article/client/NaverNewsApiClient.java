package com.spring.monew.article.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.article.domain.ArticleSource;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverNewsApiClient {

    private static final String NAVER_NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json";

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    public List<ArticleCandidate> fetchNews(String keyword, int display) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        String url = String.format("%s?query=%s&display=%d&sort=date", NAVER_NEWS_API_URL, keyword, display);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            return parseNaverResponse(response.getBody());
        } catch (Exception e) {
            log.error("네이버 API에서 뉴스를 가져오는데 실패했습니다. 키워드: {}", keyword, e);
            return new ArrayList<>();
        }
    }

    private List<ArticleCandidate> parseNaverResponse(JsonNode body) {
        List<ArticleCandidate> candidates = new ArrayList<>();

        if (body != null && body.has("items")) {
            JsonNode items = body.get("items");
            for (JsonNode item : items) {
                try {
                    String title = removeHtmlTags(item.get("title").asText());
                    String description = removeHtmlTags(item.get("description").asText());
                    String link = item.get("link").asText();
                    String pubDate = item.get("pubDate").asText();

                    Instant publishDate = parseNaverDate(pubDate);

                    ArticleCandidate candidate = ArticleCandidate.builder()
                            .source(ArticleSource.NAVER)
                            .sourceUrl(link)
                            .title(title)
                            .publishDate(publishDate)
                            .summary(description)
                            .build();

                    candidates.add(candidate);
                } catch (Exception e) {
                    log.warn("네이버 뉴스 항목 파싱 실패: {}", item, e);
                }
            }
        }

        return candidates;
    }

    private String removeHtmlTags(String text) {
        return text.replaceAll("<[^>]*>", "").replaceAll("&quot;", "\"").replaceAll("&apos;", "'")
                .replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&amp;", "&");
    }

    private Instant parseNaverDate(String pubDate) {
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        return ZonedDateTime.parse(pubDate, formatter).toInstant();
    }
}
