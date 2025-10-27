package com.spring.monew.article.batch.processor;

import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.article.domain.Article;
import com.spring.monew.interest.domain.Interest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleCandidateProcessor implements ItemProcessor<ArticleCandidate, Article> {

    private List<Interest> cachedInterests;

    @BeforeStep
    public void loadInterests(StepExecution stepExecution) {
        @SuppressWarnings("unchecked")
        List<Interest> interests = (List<Interest>) stepExecution.getExecutionContext().get("interests");
        
        cachedInterests = (interests != null && !interests.isEmpty()) ? interests : Collections.emptyList();
        log.info("키워드 매칭을 위해 {} 개의 관심사를 로드했습니다", cachedInterests.size());
    }

    @Override
    public Article process(ArticleCandidate candidate) {
        if (cachedInterests == null || cachedInterests.isEmpty()) {
            log.warn("캐시된 관심사가 없습니다. 기사를 건너뜁니다.");
            return null;
        }

        for (Interest interest : cachedInterests) {
            if (interest.getKeywords() == null || interest.getKeywords().isEmpty()) {
                continue;
            }
            
            if (matchesKeywords(candidate, interest.getKeywords())) {
                return Article.builder()
                        .interest(interest)
                        .source(candidate.getSource())
                        .sourceUrl(candidate.getSourceUrl())
                        .title(candidate.getTitle())
                        .publishDate(candidate.getPublishDate())
                        .summary(candidate.getSummary())
                        .build();
            }
        }
        
        return null;
    }

    private boolean matchesKeywords(ArticleCandidate candidate, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return false;
        }

        String title = candidate.getTitle() != null ? candidate.getTitle() : "";
        String summary = candidate.getSummary() != null ? candidate.getSummary() : "";
        String combined = (title + " " + summary).toLowerCase();
        
        if (combined.trim().isEmpty()) {
            return false;
        }

        return keywords.stream()
                .filter(kw -> kw != null && !kw.trim().isEmpty())
                .anyMatch(kw -> combined.contains(kw.toLowerCase()));
    }
}
