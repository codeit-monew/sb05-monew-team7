package com.spring.monew.articleview.service;

import java.util.UUID;

public interface ArticleViewService {
    void trackView(UUID articleId, UUID userId);
}
