package com.spring.monew.batch.scheduler;

import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.article.service.ArticleService;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCleanupScheduler {

  private static final UUID SYSTEM_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
  private static final String REQUEST_ID_KEY = "requestId";

  private final ArticleRepository articleRepository;
  private final ArticleService articleService;

  @Scheduled(cron = "0 0 15 * * *")
  public void deleteSoftDeletedArticles() {
    String batchRequestId = "BATCH-CLEANUP-" + UUID.randomUUID();
    MDC.put(REQUEST_ID_KEY, batchRequestId);

    try {
      Instant startTime = Instant.now();
      log.info("[Batch] 삭제된 게시글 정리 작업 시작");

      Instant threshold = Instant.now().minus(30, ChronoUnit.DAYS);
      List<UUID> articleIds = articleRepository.findSoftDeletedBefore(threshold);

      if (articleIds.isEmpty()) {
        log.info("[Batch] 정리할 삭제된 게시글이 없습니다.");
        return;
      }

      log.info("[Batch] 정리 대상 게시글 {}개 발견", articleIds.size());

      int successCount = 0;
      int failureCount = 0;

      for (UUID articleId : articleIds) {
        try {
          articleService.hardDeleteArticle(articleId, SYSTEM_USER_ID);
          successCount++;
        } catch (Exception e) {
          failureCount++;
          log.error("[Batch] 게시글 {} 정리 실패: {}", articleId, e.getMessage(), e);
        }
      }

      Duration executionTime = Duration.between(startTime, Instant.now());
      log.info("[Batch] 삭제된 게시글 정리 작업 완료 - 성공: {}개, 실패: {}개, 실행 시간: {}초", 
          successCount, failureCount, executionTime.getSeconds());
    } finally {
      MDC.remove(REQUEST_ID_KEY);
    }
  }
}
