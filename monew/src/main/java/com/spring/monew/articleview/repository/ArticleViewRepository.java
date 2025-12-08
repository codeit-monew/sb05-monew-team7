package com.spring.monew.articleview.repository;

import com.spring.monew.articleview.domain.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {
    boolean existsByArticleIdAndUserIdAndCreatedAtAfter(
        UUID articleId,
        UUID userId,
        Instant createdAtAfter
    );
    
    Optional<ArticleView> findTopByArticleIdAndUserIdAndCreatedAtAfterOrderByCreatedAtDesc(
        UUID articleId,
        UUID userId,
        Instant createdAtAfter
    );

    @Modifying(clearAutomatically = true)
    @Query(value = "DELETE FROM article_views WHERE article_id = :articleId", nativeQuery = true)
    void deleteByArticleId(@Param("articleId") UUID articleId);

    @Query(value = "SELECT COUNT(*) FROM article_views WHERE article_id = :articleId", nativeQuery = true)
    int countByArticleId(@Param("articleId") UUID articleId);
}
