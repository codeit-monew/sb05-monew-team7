package com.spring.monew.commentlike.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comment_like", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"comment_id", "user_id"})
})
@NoArgsConstructor @AllArgsConstructor
@Getter @Builder
public class CommentLike {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)   //PK 자동 삽입
  private UUID id;

  @Column(name = "comment_id", nullable = false)
  private UUID commentId;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
