package com.spring.monew.batch.scheduler;

import com.spring.monew.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

  private final UserService userService;

  @Scheduled(cron = "0 0 0 * * ?") // 매일 새벽 00시
  public void removeOldDeletedUsers() {
    int deleted = userService.removeUsersAfterOneDay();
    log.info("[Batch] 🧹 삭제된 유저 수:= {}", deleted);

  }
}
