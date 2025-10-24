package com.spring.monew.article.controller.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleRestoreResultDto(
        LocalDateTime restoreAt,
        List<UUID> restoredArticleIds,
        long restoredArticleCount
) {
}