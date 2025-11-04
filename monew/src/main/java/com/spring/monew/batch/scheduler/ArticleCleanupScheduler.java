package com.spring.monew.batch.scheduler;

import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.articleview.repository.ArticleViewRepository;
import com.spring.monew.comment.repository.CommentRepository;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleCleanupScheduler {

  private final ArticleRepository articleRepository;
  private final CommentRepository commentRepository;
  private final ArticleViewRepository articleViewRepository;

  @Scheduled(cron = "0 0 15 * * *")
  @Transactional
  public void deleteSoftDeletedArticles() {
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
        commentRepository.deleteCommentLikesByArticleId(articleId);
        log.debug("[Batch] 게시글 {} - 댓글 좋아요 삭제 완료", articleId);

        commentRepository.deleteByArticleId(articleId);
        log.debug("[Batch] 게시글 {} - 댓글 삭제 완료", articleId);

        articleViewRepository.deleteByArticleId(articleId);
        log.debug("[Batch] 게시글 {} - 조회 기록 삭제 완료", articleId);

        articleRepository.hardDelete(articleId);
        log.debug("[Batch] 게시글 {} - 게시글 삭제 완료", articleId);

        successCount++;
      } catch (Exception e) {
        failureCount++;
        log.error("[Batch] 게시글 {} 정리 실패: {}", articleId, e.getMessage(), e);
      }
    }

    Duration executionTime = Duration.between(startTime, Instant.now());
    log.info("[Batch] 삭제된 게시글 정리 작업 완료 - 성공: {}개, 실패: {}개, 실행 시간: {}초", 
        successCount, failureCount, executionTime.getSeconds());
  }
}
