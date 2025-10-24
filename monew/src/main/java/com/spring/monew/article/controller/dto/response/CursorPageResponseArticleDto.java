package com.spring.monew.article.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponseArticleDto(
        List<ArticleDto> content,
        String nextCursor,
        LocalDateTime nextAfterAt,
        int size,
        long totalElements,
        boolean hasNext
) {
}
