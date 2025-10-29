package com.spring.monew.article.service;

import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ArticleService {

  CursorPageResponseArticleDto getArticles(
      String keyword,
      List<String> interests,
      List<String> sources,
      Instant from,
      Instant to,
      String orderBy,
      String direction,
      String cursor,
      int limit,
      UUID userId
  );

  List<String> getSources();
}
