package com.spring.monew.notification.repository.impl;

import com.spring.monew.notification.domain.Notification;
import com.spring.monew.notification.repository.NotificationRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public List<Notification> findUnreadByUserIdWithCursor(
      UUID userId,
      Instant afterOrNow,
      Instant cursorCreatedAt,
      UUID cursorId,
      int limitPlusOne
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

  @Override
  public long countUnread(UUID userId) {
    var q = em.createQuery("""
            SELECT COUNT(n) FROM Notification n
             WHERE n.userId = :userId
               AND n.confirmed = FALSE
        """, Long.class);
    q.setParameter("userId", userId);
    return q.getSingleResult();
  }

  @Override
  public Instant getDatabaseNow() {
    // CURRENT_TIMESTAMP는 DB 서버 시각
    Timestamp ts = (Timestamp) em.createNativeQuery("select current_timestamp").getSingleResult();
    return ts.toInstant();
  }
}
