package com.spring.monew.backup.dto;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.interest.domain.Interest;

import java.time.Instant;
import java.util.UUID;

public record ArticleBackupDto(
    UUID id,
    UUID interestId,
    String source,
    String sourceUrl,
    String title,
    String summary,
    Instant publishDate,
    long viewCount,
    long commentCount,
    Instant createdAt
) {
  public static ArticleBackupDto from(Article article) {
    return new ArticleBackupDto(
        article.getId(),
        article.getInterest().getId(),
        article.getSource().name(),
        article.getSourceUrl(),
        article.getTitle(),
        article.getSummary(),
        article.getPublishDate(),
        article.getViewCount(),
        article.getCommentCount(),
        article.getCreatedAt()
    );
  }
}
