package com.spring.monew.comment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "comments")
@NoArgsConstructor @AllArgsConstructor
@Getter @Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)   //PK 자동 삽입
    private UUID id;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    @Default
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    @Default
    private long likeCount = 0L;

    // 연관 관계 (지금은 UUID로만, 이후 필요시 ManyToOne)
    // @ManyToOne(fetch = FetchType.LAZY)
    // private User user;
}
