package com.spring.monew.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "notifications",
    indexes = {
        // 커서/필터 최적화 (권장 인덱스)
        @Index(name = "idx_notif_user_confirm_created_desc", columnList = "user_id, confirmed, created_at, id")
    }
)
public class Notification {

  // 접근자
  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(name = "content", nullable = false, length = 255)
  private String content;

  @Column(name = "confirmed", nullable = false)
  private boolean confirmed;

  @Convert(converter = NotificationResourceTypeConverter.class)
  @Column(name = "resource_type") // NULL 허용
  private NotificationResourceType resourceType;

  @Column(name = "resource_id") // NULL 허용
  private UUID resourceId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  protected Notification() { /* for JPA */ }

  private Notification(UUID id,
      UUID userId,
      String content,
      boolean confirmed,
      NotificationResourceType resourceType,
      UUID resourceId,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.userId = userId;
    this.content = content;
    this.confirmed = confirmed;
    this.resourceType = resourceType;
    this.resourceId = resourceId;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  // 팩토리
  public static Notification of(UUID userId,
      String content,
      NotificationResourceType resourceType,
      UUID resourceId) {
    var now = Instant.now();
    return new Notification(
        UUID.randomUUID(),
        Objects.requireNonNull(userId, "userId"),
        Objects.requireNonNull(content, "content"),
        false, // 생성 시 미확인
        resourceType,
        resourceId,
        now,
        null
    );
  }

  // 단건 확인
  public void confirm() {
    if (!this.confirmed) {
      this.confirmed = true;
    }
  }

  // 내용 수정
  public void changeContent(String newContent) {
    this.content = Objects.requireNonNull(newContent, "content");
    this.updatedAt = Instant.now();
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Notification that)) return false;
    return Objects.equals(id, that.id);
  }
  @Override public int hashCode() { return Objects.hash(id); }

  // JPA 라이프사이클
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
