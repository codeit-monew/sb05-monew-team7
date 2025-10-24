package com.spring.monew.commentlike.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_like")
@NoArgsConstructor @AllArgsConstructor
@Getter @Builder
public class CommentLike {

  @Id
  private UUID id;

  @Column(name = "content_id", nullable = false)
  private UUID contentId; // comment_id로도 가능하지만 스키마 기준 유지

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
