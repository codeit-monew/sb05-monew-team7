package com.spring.monew.articleview.controller.dto.response;

import com.spring.monew.article.domain.ArticleSource;

import java.time.Instant;
import java.util.UUID;

public record ArticleViewDto(
        UUID id,
        UUID viewedBy,
        Instant createdAt,
        UUID articleId,
        ArticleSource source,
        String sourceUrl,
        String articleTitle,
        Instant articlePublishedDate,
        String articleSummary,
        long articleCommentCount,
        long articleViewCount
) {
}