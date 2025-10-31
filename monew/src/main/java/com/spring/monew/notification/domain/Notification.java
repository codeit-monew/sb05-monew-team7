package com.spring.monew.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
@Entity
@Table(name = "notifications")
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
  private UUID userId;

  @Column(name = "content", nullable = false, length = 255)
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "resource_type", nullable = false, length = 30)
  private NotificationResourceType resourceType;

  @Column(name = "resource_id", nullable = false, columnDefinition = "uuid")
  private UUID resourceId;

  @Column(name = "confirmed", nullable = false)
  private boolean confirmed;

  @Column(name = "created_at", nullable = false, columnDefinition = "timestamptz")
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false, columnDefinition = "timestamptz")
  private Instant updatedAt;

  public static Notification of(UUID userId, String content,
      NotificationResourceType type, UUID resourceId) {
    Notification n = new Notification();
    n.userId = userId;
    n.content = content;
    n.resourceType = type;
    n.resourceId = resourceId;
    n.confirmed = false;
    return n;
  }

  @PrePersist
  void onInsert() {
    Instant now = Instant.now();
    if (this.createdAt == null) this.createdAt = now;
    if (this.updatedAt == null) this.updatedAt = now;
  }

  @PreUpdate
  void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public void confirm() {
    this.confirmed = true;
  }
}