package com.spring.monew.article.batch.reader;

import com.spring.monew.article.client.NaverNewsApiClient;
import com.spring.monew.article.client.RssFeedClient;
import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ArticleCandidateReader implements ItemReader<ArticleCandidate> {

    private final NaverNewsApiClient naverNewsApiClient;
    private final RssFeedClient rssFeedClient;
    private final InterestRepository interestRepository;

    private List<ArticleCandidate> candidates;
    private int currentIndex;

    @BeforeStep
    public void initialize(StepExecution stepExecution) {
        currentIndex = 0;
        log.info("ArticleCandidateReader 초기화 중");
        
        candidates = new ArrayList<>();
        
        List<Interest> interests = interestRepository.findAll();
        if (interests == null || interests.isEmpty()) {
            log.warn("등록된 관심사가 없습니다. 기사 수집을 건너뜁니다.");
            stepExecution.getExecutionContext().put("interestIds", new ArrayList<String>());
            return;
        }

        List<String> interestIds = interests.stream()
                .map(interest -> interest.getId().toString())
                .collect(Collectors.toList());
        stepExecution.getExecutionContext().put("interestIds", interestIds);

        Set<String> allKeywords = new HashSet<>();
        for (Interest interest : interests) {
            List<String> keywords = interest.getKeywords();
            if (keywords != null && !keywords.isEmpty()) {
                allKeywords.addAll(keywords);
            }
        }
        
        if (allKeywords.isEmpty()) {
            log.warn("등록된 키워드가 없습니다. 기사 수집을 건너뜁니다.");
            return;
        }

        for (String keyword : allKeywords) {
            List<ArticleCandidate> naverResults = naverNewsApiClient.fetchNews(keyword, 10);
            if (naverResults != null) {
                candidates.addAll(naverResults);
            }
        }
        
        addRssResults(rssFeedClient.fetchHankyungNews());
        addRssResults(rssFeedClient.fetchChosunNews());
        addRssResults(rssFeedClient.fetchYeonhapNews());
        
        log.info("외부 소스에서 {} 개의 기사 후보를 수집했습니다", candidates.size());
    }

    private void addRssResults(List<ArticleCandidate> results) {
        if (results != null && !results.isEmpty()) {
            candidates.addAll(results);
        }
    }

    @Override
    public ArticleCandidate read() {
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }
        
        if (currentIndex >= candidates.size()) {
            return null;
        }
        
        return candidates.get(currentIndex++);
    }
}
