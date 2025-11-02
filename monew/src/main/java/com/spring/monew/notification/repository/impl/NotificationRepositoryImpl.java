package com.spring.monew.notification.repository.impl;

import com.spring.monew.notification.domain.Notification;
import com.spring.monew.notification.repository.NotificationRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  // 커서 기반
  @Override
  public List<Notification> findUnreadByUserIdWithCursor(
      UUID userId, Instant upperBoundCreatedAt, Instant cursorCreatedAt, UUID cursorId, int limitPlusOne
  ) {
    // 1 이상이어야 함
    final int safeLimit = (limitPlusOne <= 0) ? 1 : limitPlusOne;

    final StringBuilder jpql = new StringBuilder("""
        SELECT n FROM Notification n
         WHERE n.userId = :userId
           AND n.confirmed = FALSE
           AND n.createdAt <= :upperBound
        """);

    if (cursorCreatedAt != null && cursorId != null) {
      jpql.append("""
           AND ( n.createdAt < :cursorCreatedAt
              OR (n.createdAt = :cursorCreatedAt AND n.id < :cursorId) )
        """);
    }

    jpql.append(" ORDER BY n.createdAt DESC, n.id DESC");

    final TypedQuery<Notification> query = em.createQuery(jpql.toString(), Notification.class)
        .setParameter("userId", userId)
        .setParameter("upperBound", upperBoundCreatedAt)
        .setMaxResults(safeLimit);

    if (cursorCreatedAt != null && cursorId != null) {
      query.setParameter("cursorCreatedAt", cursorCreatedAt);
      query.setParameter("cursorId", cursorId);
    }

    return query.getResultList();
  }

  @Override
  public long confirmAllByUserId(UUID userId) {
    final Query q = em.createQuery("""
      UPDATE Notification n
         SET n.confirmed = TRUE,
             n.updatedAt = :now
       WHERE n.userId = :userId
         AND n.confirmed = FALSE
      """);
    q.setParameter("userId", userId);
    q.setParameter("now", java.time.Instant.now()); // 또는 getDatabaseNow()로 일관성
    return q.executeUpdate();
  }

  @Override
  public long deleteConfirmedBefore(Instant threshold) {
    final Query query = em.createQuery("""
        DELETE FROM Notification n
         WHERE n.confirmed = TRUE
           AND n.updatedAt IS NOT NULL
           AND n.updatedAt < :threshold
        """);
    query.setParameter("threshold", threshold);
    return query.executeUpdate();
  }

  @Override
  public long countUnreadByUserId(UUID userId) {
    return em.createQuery("""
      SELECT COUNT(n) FROM Notification n
       WHERE n.userId = :userId
         AND n.confirmed = FALSE
      """, Long.class)
        .setParameter("userId", userId)
        .getSingleResult();
  }

  @Override
  public Instant getDatabaseNow() {
    // 항상 하나의 컬럼
    var nativeQuery = em.createNativeQuery("select now() as db_now")
        .unwrap(org.hibernate.query.NativeQuery.class)
        .addScalar("db_now", org.hibernate.type.StandardBasicTypes.OFFSET_DATE_TIME);
    Object v = nativeQuery.getSingleResult();
    // 타입으로 내려오면 바로 변환
    if (v instanceof java.time.OffsetDateTime odt) {
      return odt.toInstant();
    }
    if (v instanceof java.time.Instant i) return i;
    if (v instanceof java.sql.Timestamp ts) return ts.toInstant();
    if (v instanceof java.time.LocalDateTime ldt) {
      return ldt.atOffset(java.time.ZoneOffset.UTC).toInstant();
    }
    throw new IllegalStateException("Unexpected DB time type: " + v + " (" + v.getClass() + ")");
  }
}