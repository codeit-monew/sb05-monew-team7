package com.spring.monew.articleview.controller.dto.response;

import com.spring.monew.article.domain.ArticleSource;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewDto(
        UUID id,
        UUID userId, // DDL: article_views.user_id
        LocalDateTime createdAt, // DDL: article_views.created_at
        UUID articleId,
        ArticleSource source,
        String sourceUrl,
        String articleTitle,
        LocalDateTime articlePublishedAt,
        String articleSummary,
        long articleCommentCount,
        long articleCount // DDL: articles.article_count
) {
}