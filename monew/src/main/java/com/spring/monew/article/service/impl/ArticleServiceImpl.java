package com.spring.monew.article.service.impl;

import com.spring.monew.article.controller.dto.response.ArticleDto;
import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.exception.ArticleNotFoundException;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.article.service.ArticleService;
import com.spring.monew.articleview.repository.ArticleViewRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleViewRepository articleViewRepository;

  @Override
  public CursorPageResponseArticleDto getArticles(
      String keyword,
      List<String> interests,
      List<String> sources,
      Instant from,
      Instant to,
      String orderBy,
      String direction,
      String cursor,
      int limit,
      UUID userId) {

    if (limit <= 0 || limit > 100) {
      throw new IllegalArgumentException("조회 개수는 1에서 100 사이여야 합니다");
    }

    if (orderBy == null || orderBy.isEmpty()) {
      orderBy = "createdAt";
    }

    if (direction == null || direction.isEmpty()) {
      direction = "DESC";
    }

    if (!List.of("publishDate", "viewCount", "commentCount", "createdAt").contains(orderBy)) {
      throw new IllegalArgumentException("잘못된 정렬 기준 필드: " + orderBy);
    }

    if (!List.of("ASC", "DESC").contains(direction.toUpperCase())) {
      throw new IllegalArgumentException("정렬 방향은 ASC 또는 DESC여야 합니다");
    }

    if (from != null && to != null && from.isAfter(to)) {
      throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다");
    }

    if (sources != null && !sources.isEmpty()) {
      for (String source : sources) {
        try {
          ArticleSource.valueOf(source);
        } catch (IllegalArgumentException e) {
          throw new IllegalArgumentException("잘못된 소스 값: " + source);
        }
      }
    }

    return articleRepository.findCursorPagedArticles(
        keyword,
        interests,
        sources,
        from,
        to,
        orderBy,
        direction.toUpperCase(),
        cursor,
        limit,
        userId
    );
  }
  @Override
  public List<String> getSources() {
    return java.util.Arrays.stream(ArticleSource.values())
        .map(Enum::name)
        .toList();
  }

  @Override
  public ArticleDto getArticle(UUID articleId, UUID userId) {
    Article article = articleRepository.findById(articleId)
        .orElseThrow(() -> new ArticleNotFoundException(articleId));

    boolean viewedByMe = false;
    if (userId != null) {
      Instant twentyFourHoursAgo = Instant.now().minusSeconds(24 * 60 * 60);
      viewedByMe = articleViewRepository.existsByArticleIdAndUserIdAndCreatedAtAfter(
          articleId, userId, twentyFourHoursAgo
      );
    }

    return new ArticleDto(
        article.getId(),
        article.getSource(),
        article.getSourceUrl(),
        article.getTitle(),
        article.getPublishDate(),
        article.getSummary(),
        article.getCommentCount(),
        article.getViewCount(),
        viewedByMe
    );
  }
}
