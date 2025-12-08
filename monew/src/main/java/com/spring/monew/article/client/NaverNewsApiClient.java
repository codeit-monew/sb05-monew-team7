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
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class NaverNewsApiClient {

    private static final String NAVER_NEWS_API_URL = "https://openapi.naver.com/v1/search/news.json";
    private static final int MAX_DISPLAY = 100;

    private final RestTemplate restTemplate;

    @Value("${naver.api.client-id}")
    private String clientId;

    @Value("${naver.api.client-secret}")
    private String clientSecret;

    public List<ArticleCandidate> fetchNews(String keyword, int display) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        int cappedDisplay = Math.min(display, MAX_DISPLAY);

        String url = UriComponentsBuilder.fromHttpUrl(NAVER_NEWS_API_URL)
                .queryParam("query", keyword)
                .queryParam("display", cappedDisplay)
                .queryParam("sort", "date")
                .encode()
                .toUriString();

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

                    description = removeEnglishTranslation(description);

                    if (!containsKorean(title) && !containsKorean(description)) {
                        log.debug("한글이 포함되지 않은 기사 제외: {}", title);
                        continue;
                    }

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

    private String removeEnglishTranslation(String text) {
        if (text == null) {
            return "";
        }
        
        text = text.replaceAll("(?i)It is assumed that there may be errors in the English translation\\.?\\s*", "");
        text = text.replaceAll("(?i)It assumes that there may be errors in the English translation\\.?\\s*", "");
        
        text = text.replaceAll("(?s)\\s*>\\s*[A-Za-z][A-Za-z0-9\\s.,;:!?'\"-]+$", "");
        
        text = cleanSummary(text);
        
        return text.trim();
    }

    private String cleanSummary(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        text = text.replaceAll("https?://[^\\s]+", "");
        text = text.replaceAll("\\(출처\\d+\\)", "");
        text = text.replaceAll("☞[^☞]*", "");
        text = text.replaceAll("\\[[^\\]]*기사\\s*모아보기[^\\]]*\\]", "");
        text = text.replaceAll("<[^>]+>", "");
        
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() 
                && !trimmed.startsWith("http") 
                && !trimmed.matches(".*%[0-9A-F]{2}.*")
                && trimmed.length() > 10
                && containsKorean(trimmed)) {
                result.append(trimmed).append(" ");
            }
        }
        
        String cleaned = result.toString().trim();
        if (cleaned.length() > 500) {
            cleaned = cleaned.substring(0, 500) + "...";
        }
        
        return cleaned;
    }

    private Instant parseNaverDate(String pubDate) {
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        return ZonedDateTime.parse(pubDate, formatter).toInstant();
    }

    private boolean containsKorean(String text) {
        return text != null && text.matches(".*[가-힣]+.*");
    }
}
