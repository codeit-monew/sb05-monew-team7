package com.spring.monew.commentlike.domain;

import com.spring.monew.comment.domain.Comment;
import com.spring.monew.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.jetbrains.annotations.TestOnly;

@Entity
@Table(name = "comment_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"comment_id", "user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CommentLike {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)   //PK 자동 삽입
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "comment_id", nullable = false)
  private Comment comment;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "created_at", nullable = false)
  @CreationTimestamp
  private Instant createdAt;

  public CommentLike(Comment comment, User user) {
    this.comment = comment;
    this.user = user;
    this.createdAt = Instant.now();
  }

  @TestOnly
  public CommentLike(UUID id, Comment comment, User user, Instant createdAt) {
    this.id = id;
    this.comment = comment;
    this.user = user;
    this.createdAt = createdAt;
  }
}
