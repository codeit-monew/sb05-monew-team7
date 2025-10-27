package com.spring.monew.article.batch.processor;

import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.article.domain.Article;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Spring Batch ItemProcessor - 뉴스 기사 후보를 검증하고 Article 엔티티로 변환하는 컴포넌트
 * 
 * [ItemProcessor의 역할]
 * ItemProcessor는 Reader에서 읽은 데이터를 가공/변환/필터링하는 중간 처리 단계입니다.
 * - 입력: Reader가 반환한 원시 데이터 (ArticleCandidate)
 * - 출력: 가공된 데이터 (Article) 또는 null (필터링)
 * 
 * [이 클래스가 하는 일]
 * 1. 기사 후보(ArticleCandidate)가 등록된 관심사(Interest)의 키워드와 매칭되는지 확인
 * 2. 매칭되면 → Article 엔티티로 변환하여 반환 (Writer가 DB에 저장)
 * 3. 매칭 안 되면 → null 반환 (해당 기사는 버려지고 저장되지 않음)
 * 
 * [null 반환의 의미]
 * Spring Batch에서 Processor가 null을 반환하면 해당 아이템은 필터링됩니다.
 * 즉, Writer로 전달되지 않고 처리 과정에서 제외됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleCandidateProcessor implements ItemProcessor<ArticleCandidate, Article> {

    private final InterestRepository interestRepository;
    private List<Interest> cachedInterests;

    @BeforeStep
    public void loadInterests() {
        cachedInterests = interestRepository.findAll();
        log.info("키워드 매칭을 위해 {} 개의 관심사를 로드했습니다", cachedInterests.size());
    }

    @Override
    public Article process(ArticleCandidate candidate) {
        for (Interest interest : cachedInterests) {
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

    /**
     * 기사 후보가 키워드와 매칭되는지 확인하는 헬퍼 메서드
     * 
     * 매칭 로직:
     * - 기사의 제목과 요약을 합쳐서 하나의 문자열로 만듦
     * - 소문자로 변환하여 대소문자 구분 없이 비교
     * - 키워드 목록 중 하나라도 포함되어 있으면 true 반환
     * 
     * @param candidate 검사할 기사 후보
     * @param keywords Interest의 키워드 목록
     * @return true 키워드가 포함되어 있으면, false 없으면
     */
    private boolean matchesKeywords(ArticleCandidate candidate, List<String> keywords) {
        String combined = (candidate.getTitle() + " " + candidate.getSummary()).toLowerCase();
        return keywords.stream().anyMatch(kw -> combined.contains(kw.toLowerCase()));
    }
}
