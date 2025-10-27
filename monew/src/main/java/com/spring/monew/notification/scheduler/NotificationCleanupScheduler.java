package com.spring.monew.notification.scheduler;

import com.spring.monew.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

  private final NotificationRepository repository;

  // 매일 03:00(UTC) 실행 - 확인되었고 updatedAt 기준 7일이 지난 알림 삭제
  @Transactional
  @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
  public void deleteOldConfirmed() {
    Instant now = repository.getDatabaseNow();
    Instant threshold = now.minus(7, ChronoUnit.DAYS);

    long deleted = repository.deleteConfirmedBefore(threshold);
    log.info("[NotificationCleanup] now={}, threshold={}, deleted={}", now, threshold, deleted);
  }
}