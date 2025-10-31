package com.spring.monew.article.controller.dto.response;

import com.querydsl.core.annotations.QueryProjection;
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
        Boolean viewedByMe
) {
    @QueryProjection
    public ArticleDto {}
}
