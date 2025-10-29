package com.spring.monew.article.client;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.article.domain.ArticleSource;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RssFeedClient {

    private static final String HANKYUNG_RSS_URL = "https://www.hankyung.com/feed/all-news";
    private static final String CHOSUN_RSS_URL = "https://www.chosun.com/arc/outboundfeeds/rss/?outputType=xml";
    private static final String YEONHAP_RSS_URL = "https://www.yna.co.kr/rss/news.xml";
    
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; MonewNewsBot/1.0)";

    public List<ArticleCandidate> fetchHankyungNews() {
        return fetchRssFeed(HANKYUNG_RSS_URL, ArticleSource.HANKYUNG);
    }

    public List<ArticleCandidate> fetchChosunNews() {
        return fetchRssFeed(CHOSUN_RSS_URL, ArticleSource.CHOSUN);
    }

    public List<ArticleCandidate> fetchYeonhapNews() {
        return fetchRssFeed(YEONHAP_RSS_URL, ArticleSource.YEONHAP);
    }

    private List<ArticleCandidate> fetchRssFeed(String feedUrl, ArticleSource source) {
        List<ArticleCandidate> candidates = new ArrayList<>();

        HttpURLConnection connection = null;
        InputStream inputStream = null;
        
        try {
            URL url = new URL(feedUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);
            connection.setRequestProperty("User-Agent", USER_AGENT);
            
            inputStream = connection.getInputStream();
            
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(inputStream));

            for (SyndEntry entry : feed.getEntries()) {
                try {
                    String title = entry.getTitle();
                    String link = entry.getLink();
                    String description = entry.getDescription() != null ? entry.getDescription().getValue() : "";
                    Date pubDate = entry.getPublishedDate();

                    if (!containsKorean(title) && !containsKorean(description)) {
                        log.debug("한글이 포함되지 않은 기사 제외: {}", title);
                        continue;
                    }

                    Instant publishDate = pubDate != null ? pubDate.toInstant() : Instant.now();

                    ArticleCandidate candidate = ArticleCandidate.builder()
                            .source(source)
                            .sourceUrl(link)
                            .title(title)
                            .publishDate(publishDate)
                            .summary(description)
                            .build();

                    candidates.add(candidate);
                } catch (Exception e) {
                    log.warn("{}에서 RSS 항목 파싱 실패: {}", source, entry, e);
                }
            }
        } catch (Exception e) {
            log.error("{}에서 RSS 피드를 가져오는데 실패했습니다: {}", source, feedUrl, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    log.warn("InputStream 닫기 실패", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        return candidates;
    }

    private boolean containsKorean(String text) {
        return text != null && text.matches(".*[가-힣]+.*");
    }
}
