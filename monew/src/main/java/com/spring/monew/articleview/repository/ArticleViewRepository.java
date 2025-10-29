package com.spring.monew.articleview.repository;

import com.spring.monew.articleview.domain.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {
    boolean existsByArticleIdAndUserIdAndCreatedAtAfter(
        UUID articleId,
        UUID userId,
        Instant createdAtAfter
    );
}
