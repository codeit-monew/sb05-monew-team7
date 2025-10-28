package com.spring.monew.batch.processor;

import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.article.domain.Article;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class ArticleCandidateProcessor implements ItemProcessor<ArticleCandidate, Article> {

    private final InterestRepository interestRepository;
    private List<Interest> cachedInterests;

    @BeforeStep
    public void loadInterests(StepExecution stepExecution) {
        @SuppressWarnings("unchecked")
        List<String> interestIdStrings = (List<String>) stepExecution.getExecutionContext().get("interestIds");
        
        if (interestIdStrings == null || interestIdStrings.isEmpty()) {
            cachedInterests = Collections.emptyList();
            log.warn("ExecutionContext에서 관심사 ID를 찾을 수 없습니다.");
            return;
        }
        
        List<UUID> interestIds = interestIdStrings.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());
        
        cachedInterests = interestRepository.findAllById(interestIds);
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
                return Article.of(
                        interest,
                        candidate.getSource(),
                        candidate.getSourceUrl(),
                        candidate.getTitle(),
                        candidate.getPublishDate(),
                        candidate.getSummary()
                );
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
