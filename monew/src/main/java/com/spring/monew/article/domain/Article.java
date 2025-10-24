package com.spring.monew.article.domain;

//import com.spring.monew.interest.domain.Interest;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE articles SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false") // 조회 시 기본적으로 삭제되지 않은 것만 조회
public class Article {

    @Id
    @Column(name = "id")
    private UUID id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "interest_id", nullable = false)
//    private Interest interest;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    private ArticleSource source;

    @Column(name = "source_url", nullable = false, unique = true, length = 255)
    private String sourceUrl;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "publish_date", nullable = false)
    private Instant publishDate;

    @Column(name = "summary", nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "comment_count", nullable = false)
    @ColumnDefault("0")
    private long commentCount = 0L;

    @Column(name = "view_count", nullable = false)
    @ColumnDefault("0")
    private long viewCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

}
