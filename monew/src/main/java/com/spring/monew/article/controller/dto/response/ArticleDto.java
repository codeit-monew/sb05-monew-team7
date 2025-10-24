package com.spring.monew.article.controller.dto.response;

import com.spring.monew.article.domain.ArticleSource;

import java.time.Instant;
import java.util.UUID;

public record ArticleDto(
        UUID id,
        ArticleSource source,
        String sourceUrl,
        String title,
        Instant publishDate,
        String summary,
        long commentCount,
        long viewCount,
        boolean viewedByMe
) {
}