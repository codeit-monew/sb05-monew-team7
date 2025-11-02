package com.spring.monew.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.monew.notification.domain.Notification;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(NotificationRepositoryTest.QuerydslTestConfig.class) // ✅ 명시적 Import
class NotificationRepositoryTest {

  // ---- 테스트 전용 QueryDSL 빈 (다른 커스텀 레포에서 필요) ----
  @TestConfiguration
  static class QuerydslTestConfig {
    @PersistenceContext EntityManager em;
    @Bean
    JPAQueryFactory jpaQueryFactory() {
      return new JPAQueryFactory(em);
    }
  }

  @Autowired NotificationRepository notificationRepository;
  @Autowired UserRepository userRepository;
  @Autowired EntityManager em;

  private static Notification make(UUID userId, String content, NotificationResourceType type) {
    return Notification.of(userId, content, type, UUID.randomUUID());
  }

  @Test
  @DisplayName("커서 기반 조회: 미확인 알림 최신순 정렬 + 커서 적용")
  void findUnreadByUserIdWithCursor() {
    // given
    User user = userRepository.save(new User("u@test.com", "u", "pw"));

    Notification n1 = notificationRepository.save(make(user.getId(), "A", NotificationResourceType.ARTICLE));
    Notification n2 = notificationRepository.save(make(user.getId(), "B", NotificationResourceType.COMMENT));
    Notification n3 = notificationRepository.save(make(user.getId(), "C", NotificationResourceType.ARTICLE));
    em.flush();

    Instant base = Instant.now();
    em.createQuery("update Notification n set n.createdAt = :t where n.id = :id")
        .setParameter("t", base.minusSeconds(2)).setParameter("id", n1.getId()).executeUpdate();
    em.createQuery("update Notification n set n.createdAt = :t where n.id = :id")
        .setParameter("t", base.minusSeconds(1)).setParameter("id", n2.getId()).executeUpdate();
    em.createQuery("update Notification n set n.createdAt = :t where n.id = :id")
        .setParameter("t", base).setParameter("id", n3.getId()).executeUpdate();
    em.flush();
    em.clear();

    Instant upper = Instant.now().plusSeconds(5);

    // when
    List<Notification> first = notificationRepository.findUnreadByUserIdWithCursor(
        user.getId(), upper, null, null, 3 + 1);

    // then
    assertThat(first).hasSize(3);

    // 정렬: createdAt DESC, (동시각이면) id DESC
    for (int i = 0; i < first.size() - 1; i++) {
      var a = first.get(i);
      var b = first.get(i + 1);
      int cmp = a.getCreatedAt().compareTo(b.getCreatedAt());
      if (cmp < 0) throw new AssertionError("createdAt must be DESC");
      if (cmp == 0) {
        assertThat(a.getId().toString().compareTo(b.getId().toString()))
            .as("id must be DESC when createdAt equal")
            .isGreaterThan(0);
      }
    }

    // 커서 = 마지막 요소
    var last = first.get(first.size() - 1);
    List<Notification> second = notificationRepository.findUnreadByUserIdWithCursor(
        user.getId(), upper, last.getCreatedAt(), last.getId(), 3 + 1);

    assertThat(second.size()).isBetween(0, 1);
  }

  @Test
  @DisplayName("전체 확인 시나리오(레포 메서드 호출 없이 JPQL로 시뮬레이션) 후 unread=0")
  void simulateConfirmAll_unreadBecomesZero() {
    // given
    User user = userRepository.save(new User("u2@test.com", "u2", "pw"));
    notificationRepository.saveAll(List.of(
        make(user.getId(), "A", NotificationResourceType.ARTICLE),
        make(user.getId(), "B", NotificationResourceType.COMMENT)
    ));
    em.flush();
    em.clear();
    assertThat(notificationRepository.countUnreadByUserId(user.getId())).isEqualTo(2);

    // when: H2 Timestamp↔Instant 이슈 회피 — JPQL로 직접 업데이트
    Instant now = Instant.now();
    em.createQuery("""
        update Notification n
           set n.confirmed = true,
               n.updatedAt = :now
         where n.userId = :uid and n.confirmed = false
        """)
        .setParameter("now", now)
        .setParameter("uid", user.getId())
        .executeUpdate();
    em.flush();
    em.clear();

    // then
    assertThat(notificationRepository.countUnreadByUserId(user.getId())).isZero();
    assertThat(notificationRepository.existsByUserIdAndConfirmedFalse(user.getId())).isFalse();
  }

  @Test
  @DisplayName("deleteConfirmedBefore: 기준 이전 confirmed 알림 물리 삭제 (JPQL로 updatedAt 과거화)")
  void deleteConfirmedBefore() {
    // given
    User user = userRepository.save(new User("u3@test.com", "u3", "pw"));
    Notification n1 = notificationRepository.save(make(user.getId(), "A", NotificationResourceType.ARTICLE));
    Notification n2 = notificationRepository.save(make(user.getId(), "B", NotificationResourceType.COMMENT));
    em.flush();

    // 모두 확인 처리
    Instant now = Instant.now();
    em.createQuery("""
        update Notification n
           set n.confirmed = true,
               n.updatedAt = :now
         where n.userId = :uid and n.confirmed = false
        """)
        .setParameter("now", now)
        .setParameter("uid", user.getId())
        .executeUpdate();
    em.flush();

    // updatedAt 과거화
    Instant past = Instant.now().minus(Duration.ofHours(2));
    em.createQuery("update Notification n set n.updatedAt = :past where n.id = :id")
        .setParameter("past", past)
        .setParameter("id", n1.getId())
        .executeUpdate();
    em.flush();
    em.clear();

    // when: 기준(threshold) 1시간 전 → n1만 삭제
    Instant threshold = Instant.now().minus(Duration.ofHours(1));
    long deleted = notificationRepository.deleteConfirmedBefore(threshold);

    // then
    assertThat(deleted).isEqualTo(1);
    assertThat(notificationRepository.findById(n1.getId())).isEmpty();
    assertThat(notificationRepository.findById(n2.getId())).isPresent();
  }

  // getDatabaseNow()는 네이티브 타입 매핑 이슈로 레포 단위 테스트에서 제외.

  private static void sleep(long millis) {
    try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
  }
}
