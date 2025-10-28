package com.spring.monew.notification.repository;

import com.spring.monew.notification.domain.Notification;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

//커서 기반 조회·일괄 갱신 등 JPA 메서드로 표현하기 애매한 쿼리 모음.
public interface NotificationRepositoryCustom {

  // 특정 사용자의 "읽지 않은(unconfirmed=false)" 알림을 커서 기반으로 조회합니다.
  List<Notification> findUnreadByUserIdWithCursor(
      UUID userId,
      Instant upperBoundCreatedAt,
      Instant cursorCreatedAt,
      UUID cursorId,
      int limitPlusOne
  );

  // 사용자 알림을 일괄 확인 처리합니다(confirmed=true).
  long confirmAllByUserId(UUID userId);

  long countUnreadByUserId(UUID userId);

  // 기준 시각 이전의 "확인된(confirmed=true)" 알림을 물리 삭제합니다.
  long deleteConfirmedBefore(Instant threshold);

  // DB 서버가 인식하는 현재 시각(UTC 기준)을 조회합니다.
  Instant getDatabaseNow();
}