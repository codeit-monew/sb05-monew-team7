package com.spring.monew.article.batch.reader;

import com.spring.monew.article.client.NaverNewsApiClient;
import com.spring.monew.article.client.RssFeedClient;
import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Spring Batch ItemReader - 뉴스 기사 수집을 위한 데이터 읽기 컴포넌트
 * 
 * [Spring Batch 기본 개념]
 * Spring Batch는 대량의 데이터를 처리하기 위한 프레임워크입니다.
 * 배치 작업은 다음 3단계로 구성됩니다:
 * 1. ItemReader (읽기) - 데이터를 어디서 가져올지 정의
 * 2. ItemProcessor (처리) - 읽은 데이터를 어떻게 가공할지 정의
 * 3. ItemWriter (쓰기) - 가공한 데이터를 어디에 저장할지 정의
 * 
 * [이 클래스의 역할]
 * 이 Reader는 외부 뉴스 소스(네이버 API, RSS 피드)에서 기사 후보들을 수집하여
 * 하나씩 반환하는 역할을 합니다.
 * 
 * [동작 방식]
 * 1. @BeforeStep: Step이 시작되기 전에 모든 뉴스를 한 번에 가져와서 메모리에 저장
 * 2. read(): Spring Batch가 호출할 때마다 저장된 뉴스를 하나씩 반환
 * 3. null 반환: 더 이상 읽을 데이터가 없으면 null을 반환하여 읽기 종료를 알림
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ArticleCandidateReader implements ItemReader<ArticleCandidate> {

    private final NaverNewsApiClient naverNewsApiClient;
    private final RssFeedClient rssFeedClient;
    private final InterestRepository interestRepository;

    private List<ArticleCandidate> candidates;
    private int currentIndex = 0;

    /**
     * Step 시작 전에 실행되는 초기화 메서드
     * 
     * @BeforeStep 어노테이션:
     * - Spring Batch가 Step을 시작하기 직전에 이 메서드를 자동으로 호출합니다
     * - 데이터 소스 연결, 파일 열기, API 호출 등의 준비 작업을 여기서 수행합니다
     * 
     * 이 메서드가 하는 일:
     * 1. DB에서 등록된 모든 관심사(Interest)의 키워드를 조회
     * 2. 각 키워드로 네이버 뉴스 API를 호출하여 관련 기사 수집
     * 3. RSS 피드(한경, 조선, 연합)에서 최신 기사 수집
     * 4. 모든 기사 후보를 candidates 리스트에 저장
     */
    @BeforeStep
    public void initialize() {
        log.info("ArticleCandidateReader 초기화 중");
        
        candidates = new ArrayList<>();
        
        List<Interest> interests = interestRepository.findAll();
        Set<String> allKeywords = new HashSet<>();
        for (Interest interest : interests) {
            allKeywords.addAll(interest.getKeywords());
        }
        
        for (String keyword : allKeywords) {
            List<ArticleCandidate> naverResults = naverNewsApiClient.fetchNews(keyword, 10);
            candidates.addAll(naverResults);
        }
        
        candidates.addAll(rssFeedClient.fetchHankyungNews());
        candidates.addAll(rssFeedClient.fetchChosunNews());
        candidates.addAll(rssFeedClient.fetchYeonhapNews());
        
        log.info("외부 소스에서 {} 개의 기사 후보를 수집했습니다", candidates.size());
    }

    /**
     * Spring Batch가 데이터를 읽을 때 호출하는 메서드
     * 
     * Spring Batch 동작 원리:
     * - Spring Batch는 이 메서드를 반복적으로 호출합니다
     * - 매번 호출될 때마다 다음 아이템(기사)을 하나씩 반환합니다
     * - null을 반환하면 "더 이상 읽을 데이터가 없다"는 신호로 인식하고 읽기를 종료합니다
     * 
     * @return ArticleCandidate 다음 기사 후보, 없으면 null
     */
    @Override
    public ArticleCandidate read() {
        if (currentIndex < candidates.size()) {
            return candidates.get(currentIndex++);
        }
        return null;
    }
}
