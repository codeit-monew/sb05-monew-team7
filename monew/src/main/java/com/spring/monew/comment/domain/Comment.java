package com.spring.monew.comment.domain;

import com.spring.monew.article.domain.Article;
import com.spring.monew.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@SQLDelete(sql = "UPDATE comments SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("is_deleted = false") // 조회 시 기본적으로 삭제되지 않은 것만 조회
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)   //PK 자동 삽입
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", nullable = false)
     private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "is_deleted", nullable = false)
    @ColumnDefault("false")
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "like_count", nullable = false)
    @ColumnDefault("0")
    private long likeCount;

    public Comment(Article article, User user,String content) {
        this.article = article;
        this.user = user;
        this.content = content;
        this.createdAt = Instant.now();
    }

    public void update(String content) {
        if (content != null &&  !content.isEmpty()) this.content = content;
    }

}
