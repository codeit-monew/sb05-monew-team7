package com.spring.monew.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(
    name = "notifications",
    indexes = {
        @Index(
            name = "idx_notif_user_confirm_created_desc",
            columnList = "user_id, confirmed, created_at, id"
        )
    }
)
public class Notification {

  // getters
  @Id
  @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "content", nullable = false, length = 255)
  private String content;

  @Column(name = "confirmed", nullable = false)
  private boolean confirmed;

  // PostgreSQL enum 타입을 사용할 때: NAMED_ENUM + columnDefinition
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "resource_type", columnDefinition = "resource_type")
  private NotificationResourceType resourceType; // nullable 허용

  @Column(name = "resource_id", columnDefinition = "uuid")
  private UUID resourceId; // nullable 허용

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  protected Notification() { /* for JPA */ }

  private Notification(
      UUID id,
      UUID userId,
      String content,
      boolean confirmed,
      NotificationResourceType resourceType,
      UUID resourceId,
      Instant createdAt,
      Instant updatedAt
  ) {
    this.id = id;
    this.userId = userId;
    this.content = content;
    this.confirmed = confirmed;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // 정적 팩토리: 필수값만 받고, 길이/널 검증을 도메인에서 보장
  public static Notification of(
      UUID userId,
      String content,
      NotificationResourceType resourceType,
      UUID resourceId
  ) {
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(content, "content");

    // 내용 정리(가독/안전): 255자 제한
    String normalized = content.trim();
    if (normalized.length() > 255) {
      throw new IllegalArgumentException("Content length must not exceed 255 characters");
    }

    Instant now = Instant.now();
    return new Notification(
        UUID.randomUUID(),
        userId,
        normalized,
        false,               // 최초 생성 시 미확인
        resourceType,        // nullable 허용
        resourceId,          // nullable 허용
        now,
        null
    );
  }

  /** 단건 확인: 변경만 수행(서비스에서 flush 시 @PreUpdate로 updatedAt 반영) */
  public void confirm() {
    if (!this.confirmed) {
      this.confirmed = true;
    }
  }

  /** 내용 수정(유효성 보장) */
  public void changeContent(String newContent) {
    Objects.requireNonNull(newContent, "content");
    String normalized = newContent.trim();
    if (normalized.length() > 255) {
      throw new IllegalArgumentException("Content length must not exceed 255 characters");
    }
    this.content = normalized;
  }

  // equality: 식별자 기반
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Notification that)) return false;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  // JPA 라이프사이클: DB 저장 시각 일관성 보장
  @PrePersist
  void onCreate() {
    if (this.id == null) this.id = UUID.randomUUID();
    if (this.createdAt == null) this.createdAt = Instant.now();
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }
}
