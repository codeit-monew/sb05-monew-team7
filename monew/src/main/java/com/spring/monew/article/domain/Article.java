package com.spring.monew.article.domain;

import com.spring.monew.interest.domain.Interest;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

import java.time.Instant;
import java.util.UUID;
import org.jetbrains.annotations.TestOnly;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE articles SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Article {

    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_id", nullable = false)
    private Interest interest;

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

    private Article(Interest interest, ArticleSource source, String sourceUrl, 
                   String title, Instant publishDate, String summary) {
        this.id = UUID.randomUUID();
        this.interest = interest;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.publishDate = publishDate;
        this.summary = summary;
    }

    public static Article of(Interest interest, ArticleSource source, String sourceUrl,
                             String title, Instant publishDate, String summary) {
        return new Article(interest, source, sourceUrl, title, publishDate, summary);
    }

    public static Article restore(UUID id, Interest interest, ArticleSource source,
                                  String sourceUrl, String title, Instant publishDate,
                                  String summary, long viewCount, long commentCount,
                                  Instant createdAt) {
      Article article = new Article(interest, source, sourceUrl, title, publishDate, summary);
      article.id = id;
      article.viewCount = viewCount;
      article.commentCount = commentCount;
      article.createdAt = createdAt;
      return article;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
    
    
    @TestOnly
    public Article(
        UUID id,
        Interest interest,
        ArticleSource source,
        String sourceUrl,
        String title,
        Instant publishDate,
        String summary,
        long commentCount,
        long viewCount,
        boolean isDeleted,
        Instant createdAt,
        Instant updatedAt
    ) {
        this.id = id;
        this.interest = interest;
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.publishDate = publishDate;
        this.summary = summary;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.isDeleted = isDeleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
