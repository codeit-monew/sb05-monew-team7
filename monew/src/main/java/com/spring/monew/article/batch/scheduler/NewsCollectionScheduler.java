package com.spring.monew.article.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 뉴스 수집 배치 작업을 정기적으로 실행하는 스케줄러
 *
 * <h3>Spring Batch 스케줄러란?</h3>
 * Spring Batch Job은 수동으로 실행할 수도 있지만, 대부분의 경우 정해진 시간에 자동으로
 * 실행되어야 합니다. 이 스케줄러는 Spring의 @Scheduled 어노테이션을 사용하여
 * 배치 작업을 주기적으로 자동 실행합니다.
 *
 * <h3>스케줄러와 배치의 관계</h3>
 * <pre>
 * ┌─────────────────────┐
 * │  스케줄러           │
 * │  (매시 정각 실행)   │  ─────┐
 * └─────────────────────┘       │
 *                               │ JobLauncher.run()
 *                               ▼
 * ┌─────────────────────────────────────────┐
 * │  Spring Batch Job                       │
 * │  (Reader → Processor → Writer)          │
 * └─────────────────────────────────────────┘
 * </pre>
 *
 * <h3>주요 구성 요소</h3>
 * <ul>
 *   <li><b>JobLauncher</b>: Spring Batch Job을 실행시키는 역할
 *       - 마치 자동차의 시동 버튼과 같은 역할
 *       - run() 메서드로 Job을 시작합니다</li>
 *   <li><b>Job</b>: 실제로 실행될 배치 작업 (NewsCollectionJob)
 *       - NewsCollectionJobConfig에서 정의한 Job 빈을 주입받습니다</li>
 *   <li><b>JobParameters</b>: Job 실행시 전달하는 파라미터
 *       - 각 실행을 구분하는 고유 식별자 역할
 *       - 동일한 파라미터로는 Job을 재실행할 수 없습니다</li>
 * </ul>
 *
 * <h3>왜 매번 다른 JobParameters가 필요한가?</h3>
 * Spring Batch는 Job 실행 이력을 DB에 저장하여 관리합니다.
 * 동일한 파라미터로 Job을 실행하면 "이미 성공적으로 완료된 Job"으로 간주하여
 * 재실행을 방지합니다. 따라서 매 실행마다 고유한 파라미터(여기서는 현재 시각)를
 * 추가하여 각 실행을 구분합니다.
 *
 * <h3>실행 주기</h3>
 * cron = "0 0 * * * *" 의미:
 * <pre>
 * 초 분 시 일 월 요일
 * 0  0  *  *  *  *    ← 매시 정각 (00분 00초)
 *
 * 예시: 01:00:00, 02:00:00, 03:00:00, ...
 * </pre>
 *
 * @see com.spring.monew.article.batch.config.NewsCollectionJobConfig
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewsCollectionScheduler {

    /**
     * Spring Batch Job을 실행하는 런처
     *
     * JobLauncher는 Spring Batch 프레임워크가 자동으로 제공하는 빈입니다.
     * 이것을 사용하여 Job을 실행하면:
     * 1. Job 실행 이력이 DB에 자동 저장됩니다 (BATCH_JOB_EXECUTION 테이블)
     * 2. 실패한 Job의 재시작이 가능합니다
     * 3. Job의 상태(성공/실패)를 추적할 수 있습니다
     */
    private final JobLauncher jobLauncher;

    /**
     * 실행할 배치 작업 (NewsCollectionJobConfig에서 정의)
     *
     * 이 Job은 다음 단계로 구성되어 있습니다:
     * 1. ArticleCandidateReader: 뉴스 API에서 기사 후보 조회
     * 2. ArticleCandidateProcessor: 키워드 기반 필터링
     * 3. ArticleWriter: 유효한 기사를 DB에 저장
     */
    private final Job newsCollectionJob;

    /**
     * 매시 정각마다 뉴스 수집 배치 작업을 실행합니다.
     *
     * <h3>실행 흐름</h3>
     * <pre>
     * 1. 매시 정각이 되면 이 메서드가 자동 호출됩니다
     *    ↓
     * 2. 고유한 JobParameters를 생성합니다 (현재 시각 추가)
     *    ↓
     * 3. JobLauncher.run()으로 배치 작업을 시작합니다
     *    ↓
     * 4. Spring Batch가 Job → Step → Reader/Processor/Writer 순서로 실행
     *    ↓
     * 5. 성공하면 로그 출력, 실패하면 에러 로그 출력
     * </pre>
     *
     * <h3>에러 처리 전략</h3>
     * try-catch로 예외를 잡아 로그만 출력하고 계속 진행합니다.
     * 이렇게 하는 이유:
     * - 한 번 실패해도 다음 시간에 다시 시도할 수 있습니다
     * - 스케줄러가 중단되지 않고 계속 동작합니다
     * - 실패 원인은 로그에서 확인하여 수동으로 조치합니다
     *
     * <h3>JobParameters의 역할</h3>
     * addDate("startTime", new Date())를 추가하는 이유:
     * - 매 실행마다 다른 파라미터를 생성하여 각 실행을 구분합니다
     * - Spring Batch는 동일한 파라미터로 성공한 Job을 재실행하지 않습니다
     * - 따라서 매번 변하는 값(현재 시각)을 파라미터로 추가합니다
     */
    @Scheduled(cron = "0 0 * * * *")
    public void runNewsCollection() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addDate("startTime", new Date())
                    .toJobParameters();

            log.info("뉴스 수집 배치 작업 시작");
            jobLauncher.run(newsCollectionJob, params);
            log.info("뉴스 수집 배치 작업 완료");
        } catch (Exception e) {
            log.error("뉴스 수집 배치 작업 실행 실패", e);
        }
    }
}
