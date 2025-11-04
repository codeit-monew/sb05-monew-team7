package com.spring.monew.article.service;

import com.spring.monew.article.controller.dto.response.ArticleDto;
import com.spring.monew.article.controller.dto.response.ArticleRestoreResultDto;
import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ArticleService {

  CursorPageResponseArticleDto getArticles(
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

  List<String> getSources();

  ArticleDto getArticle(UUID articleId, UUID userId);

  void softDeleteArticle(UUID articleId, UUID userId);

  void hardDeleteArticle(UUID articleId, UUID userId);

  ArticleRestoreResultDto restoreArticlesFromBackup(Instant fromDate, Instant toDate, UUID userId);
}
