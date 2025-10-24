package com.spring.monew.article.controller.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ArticleRestoreResultDto(
        Instant restoreDate,
        List<UUID> restoredArticleIds,
        long restoredArticleCount
) {
}