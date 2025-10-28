package com.spring.monew.article.batch.scheduler;

import com.spring.monew.comment.repository.CommentRepository;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentCleanupScheduler {

  private final CommentRepository commentRepository;

  @Scheduled(cron = "0 0 0 * * *") // 매일 00시에 스케쥴링 작동
  @Transactional
  public void deleteSoftDeletedComments() {

    // 1시간전 처리
    Instant threshold = Instant.now().minus(1, ChronoUnit.HOURS);

    int deletedCount = commentRepository.deleteSoftDeletedBefore(threshold);

    log.info("[Batch] Hard Deleted Comments = {}", deletedCount);
  }
}

