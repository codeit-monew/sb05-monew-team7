package com.spring.monew.notification.scheduler;

import com.spring.monew.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

  private final NotificationRepository repository;

  // 동시 실행 방지용 락
  private static final ReentrantLock CLEANUP_LOCK = new ReentrantLock();

  // 무한 루프 방지 안전장치(반복 삭제 상한)
  private static final int MAX_ITERATIONS = 10;

  // 매일 03:00(UTC) 실행 - 확인되었고 updatedAt 기준 7일이 지난 알림 삭제
  @Transactional
  @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
  public void deleteOldConfirmed() {
    // 다른 실행이 진행 중이면 스킵
    boolean acquired = false;
    try {
      acquired = CLEANUP_LOCK.tryLock(0, TimeUnit.SECONDS);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      return;
    }
    if (!acquired) {
      log.info("[NotificationCleanup] skipped: previous run still in progress");
      return;
    }

    final Instant start = Instant.now();
    try {
      // DB now() 먼저
      Instant now;
      try {
        now = repository.getDatabaseNow();
      } catch (Exception ex) {
        now = Instant.now();
        log.warn("[NotificationCleanup] getDatabaseNow() failed, fallback to Instant.now()", ex);
      }

      final Instant threshold = now.minus(7, ChronoUnit.DAYS);

      long totalDeleted = 0L;
      int iterations = 0;

      while (iterations < MAX_ITERATIONS) {
        iterations++;
        long deleted = repository.deleteConfirmedBefore(threshold);
        totalDeleted += deleted;
        // 더 이상 삭제할 것이 없으면 종료
        if (deleted == 0L) break;
      }

      log.info(
          "[NotificationCleanup] now={}, threshold={}, deletedTotal={}, iterations={}, duration={}",
          now, threshold, totalDeleted, iterations, Duration.between(start, Instant.now())
      );

    } catch (Exception e) {
      log.error("[NotificationCleanup] failed", e);
      //예외 전파 시 롤백
      throw e;
    } finally {
      CLEANUP_LOCK.unlock();
    }
  }
}