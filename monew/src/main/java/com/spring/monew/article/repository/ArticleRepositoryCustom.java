package com.spring.monew.article.repository;

import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ArticleRepositoryCustom {

  CursorPageResponseArticleDto findCursorPagedArticles(
      String keyword,
      List<String> interests,
      List<String> sources,
      Instant publishDateFrom,
      Instant publishDateTo,
      String orderBy,
      String direction,
      String cursor,
      int limit,
      UUID userId
  );
}
