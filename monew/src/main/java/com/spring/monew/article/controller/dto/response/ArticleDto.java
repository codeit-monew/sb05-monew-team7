package com.spring.monew.article.controller.dto.response;

import com.spring.monew.article.domain.ArticleSource;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleDto(
        UUID id,
        ArticleSource source,
        String sourceUrl,
        String title,
        LocalDateTime publishAt, // DDL: publish_date (TIMESTAMPTZ)
        String summary,
        long commentCount,  // DDL: comment_count (BIGINT)
        long articleCount,  // DDL: article_count (BIGINT)
        boolean viewedByMe
) {
}