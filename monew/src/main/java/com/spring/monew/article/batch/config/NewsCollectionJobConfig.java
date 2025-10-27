package com.spring.monew.article.batch.config;

import com.spring.monew.article.batch.processor.ArticleCandidateProcessor;
import com.spring.monew.article.batch.reader.ArticleCandidateReader;
import com.spring.monew.article.batch.writer.ArticleWriter;
import com.spring.monew.article.client.dto.ArticleCandidate;
import com.spring.monew.article.domain.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch Job 설정 클래스 - 뉴스 수집 배치 작업을 정의
 * 
 * [Spring Batch 핵심 용어]
 * 
 * 1. Job (일감, 작업)
 *    - 배치 처리의 가장 큰 단위
 *    - 하나 이상의 Step으로 구성됨
 *    - 예: "뉴스 수집 Job"은 "뉴스 수집 Step"을 포함
 * 
 * 2. Step (단계)
 *    - Job을 구성하는 독립적인 처리 단위
 *    - 각 Step은 Reader, Processor, Writer로 구성
 *    - 여러 Step을 순차적으로 실행 가능 (Step1 → Step2 → Step3)
 * 
 * 3. Chunk (청크)
 *    - 트랜잭션 단위로 처리할 아이템의 개수
 *    - chunk(10) → 10개씩 읽어서 처리하고 저장 후 커밋
 *    - 성능과 메모리 효율을 위한 설정
 * 
 * [이 설정 클래스가 하는 일]
 * - newsCollectionJob: 뉴스 수집이라는 "작업(Job)" 정의
 * - newsCollectionStep: 뉴스를 읽고→처리하고→저장하는 "단계(Step)" 정의
 * 
 * [의존성 주입]
 * - JobRepository: Spring Batch가 Job 실행 이력을 저장하는 저장소
 * - TransactionManager: Chunk 단위로 트랜잭션을 관리
 * - Reader, Processor, Writer: 우리가 만든 배치 컴포넌트들
 */
@Configuration
@RequiredArgsConstructor
public class NewsCollectionJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ArticleCandidateReader articleCandidateReader;
    private final ArticleCandidateProcessor articleCandidateProcessor;
    private final ArticleWriter articleWriter;

    /**
     * 뉴스 수집 Job 정의
     * 
     * Job의 구성:
     * - 이름: "newsCollectionJob" (JobRepository에 이 이름으로 저장됨)
     * - 시작 Step: newsCollectionStep() (현재는 Step이 하나지만 여러 개 추가 가능)
     * 
     * Job 실행 흐름:
     * 1. Scheduler가 매시간 이 Job을 실행 요청
     * 2. Spring Batch가 newsCollectionStep 실행
     * 3. Step 완료 후 Job 종료
     * 4. 실행 이력이 JobRepository에 자동 저장 (성공/실패, 실행 시간 등)
     * 
     * @return Job Spring Batch가 실행할 Job 객체
     */
    @Bean
    public Job newsCollectionJob() {
        return new JobBuilder("newsCollectionJob", jobRepository)
                .start(newsCollectionStep())
                .build();
    }

    /**
     * 뉴스 수집 Step 정의
     * 
     * Step의 구성 요소:
     * 1. chunk(10): 10개씩 묶어서 처리 (청크 사이즈)
     * 2. reader: ArticleCandidateReader - 외부 API/RSS에서 뉴스 읽기
     * 3. processor: ArticleCandidateProcessor - 키워드 매칭 및 변환
     * 4. writer: ArticleWriter - DB에 저장
     * 
     * Chunk 처리 흐름 (chunk size = 10일 때):
     * ┌─────────────────────────────────────────┐
     * │ 1. Reader가 read()를 10번 호출          │
     * │    → ArticleCandidate 10개 수집         │
     * ├─────────────────────────────────────────┤
     * │ 2. Processor가 process()를 10번 호출    │
     * │    → 각각 Article로 변환 (일부는 null)  │
     * ├─────────────────────────────────────────┤
     * │ 3. Writer가 write()를 1번 호출          │
     * │    → null 제외한 Article들을 한 번에 저장│
     * ├─────────────────────────────────────────┤
     * │ 4. 트랜잭션 커밋                        │
     * └─────────────────────────────────────────┘
     * 
     * 이 과정이 Reader가 null을 반환할 때까지 반복됨
     * 
     * 제네릭 타입 설명:
     * - <ArticleCandidate, Article>
     * - 첫 번째: Reader가 읽는 타입 (입력)
     * - 두 번째: Processor가 반환하고 Writer가 받는 타입 (출력)
     * 
     * @return Step Spring Batch가 실행할 Step 객체
     */
    @Bean
    public Step newsCollectionStep() {
        return new StepBuilder("newsCollectionStep", jobRepository)
                .<ArticleCandidate, Article>chunk(10, transactionManager)
                .reader(articleCandidateReader)
                .processor(articleCandidateProcessor)
                .writer(articleWriter)
                .faultTolerant()
                .skip(DataIntegrityViolationException.class)
                .skipLimit(Integer.MAX_VALUE)
                .build();
    }
}
