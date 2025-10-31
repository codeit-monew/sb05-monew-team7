package com.spring.monew.articleview.service;

import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import java.util.UUID;

public interface ArticleViewService {
    ArticleViewDto trackView(UUID articleId, UUID userId);
}
