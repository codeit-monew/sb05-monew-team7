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
        // 목록 조회용(사용자별 최신순) +  id
        @Index(name = "idx_notif_user_created_id_desc",
            columnList = "user_id, created_at, id"),
        // 확인여부 + 갱신시각
        @Index(name = "idx_notif_confirmed_updated",
            columnList = "confirmed, updated_at")
    }
)
public class Notification {

  @Id
  @Column(name = "id", nullable = false, updatable = false, columnDefinition = "uuid")
  private UUID id;

  @Column(name = "user_id", nullable = false, updatable = false, columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "content", nullable = false, length = 255)
  private String content;

  @Column(name = "confirmed", nullable = false)
  private boolean confirmed;

  // PostgreSQL ENUM 사용 시
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Column(name = "resource_type", nullable = false, columnDefinition = "resource_type")
  private NotificationResourceType resourceType; // NOT NULL

  @Column(name = "resource_id", nullable = false, columnDefinition = "uuid")
  private UUID resourceId; // NOT NULL

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  protected Notification() {}

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

  // 필수값 검증
  public static Notification of(
      UUID userId,
      String content,
      NotificationResourceType resourceType,
      UUID resourceId
  ) {
    Objects.requireNonNull(userId, "userId");
    Objects.requireNonNull(content, "content");
    Objects.requireNonNull(resourceType, "resourceType");
    Objects.requireNonNull(resourceId, "resourceId");

    String normalized = content.trim();
    if (normalized.length() > 255) {
      throw new IllegalArgumentException("Content length must not exceed 255 characters");
    }

    Instant now = Instant.now();
    return new Notification(
        UUID.randomUUID(),
        userId,
        normalized,
        false,
        resourceType,
        resourceId,
        now,
        null
    );
  }

  public void confirm() {
    if (!this.confirmed) {
      this.confirmed = true;
    }
  }

  public void changeContent(String newContent) {
    Objects.requireNonNull(newContent, "content");
    String normalized = newContent.trim();
    if (normalized.length() > 255) {
      throw new IllegalArgumentException("Content length must not exceed 255 characters");
    }
    this.content = normalized;
  }

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
