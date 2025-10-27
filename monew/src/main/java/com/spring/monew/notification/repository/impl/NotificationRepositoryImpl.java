package com.spring.monew.notification.repository.impl;

import com.spring.monew.notification.domain.Notification;
import com.spring.monew.notification.repository.NotificationRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  // ===== 이미 너에게 있던 메서드들 =====
  @Override
  public List<Notification> findUnreadByUserIdWithCursor(
      UUID userId, Instant afterOrNow, Instant cursorCreatedAt, UUID cursorId, int limitPlusOne
  ) {
    var jpql = new StringBuilder("""
                SELECT n FROM Notification n
                 WHERE n.userId = :userId
                   AND n.confirmed = FALSE
                   AND n.createdAt <= :after
                """);

    if (cursorCreatedAt != null && cursorId != null) {
      jpql.append("""
                   AND ( n.createdAt < :cursorCreatedAt
                      OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId) )
                """);
    }

    jpql.append(" ORDER BY n.createdAt DESC, n.id DESC");

    var q = em.createQuery(jpql.toString(), Notification.class)
        .setParameter("userId", userId)
        .setParameter("after", afterOrNow)
        .setMaxResults(limitPlusOne);

    if (cursorCreatedAt != null && cursorId != null) {
      q.setParameter("cursorCreatedAt", cursorCreatedAt);
      q.setParameter("cursorId", cursorId);
    }

    return q.getResultList();
  }

  @Override
  public long confirmAllByUserId(UUID userId) {
    var q = em.createQuery("""
            UPDATE Notification n
               SET n.confirmed = TRUE,
                   n.updatedAt = CURRENT_TIMESTAMP
             WHERE n.userId = :userId
               AND n.confirmed = FALSE
        """);
    q.setParameter("userId", userId);
    return q.executeUpdate();
  }

  // ===== 새로 추가되는 메서드들 =====

  @Override
  public long deleteConfirmedBefore(Instant threshold) {
    var q = em.createQuery("""
            DELETE FROM Notification n
             WHERE n.confirmed = TRUE
               AND n.updatedAt IS NOT NULL
               AND n.updatedAt < :threshold
        """);
    q.setParameter("threshold", threshold);
    return q.executeUpdate();
  }

  @Override
  public Instant getDatabaseNow() {
    // DB의 CURRENT_TIMESTAMP 사용
    return em.createQuery("SELECT CURRENT_TIMESTAMP FROM Notification n", Instant.class)
        .setMaxResults(1)
        .getSingleResult();
  }
}