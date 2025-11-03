package com.spring.monew.article.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.article.controller.dto.response.ArticleDto;
import com.spring.monew.article.controller.dto.response.ArticleRestoreResultDto;
import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.exception.ArticleNotFoundException;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.article.service.ArticleService;
import com.spring.monew.backup.dto.ArticleBackupDto;
import com.spring.monew.backup.service.S3BackupService;
import com.spring.monew.common.logging.AuditLogger;
import com.spring.monew.articleview.repository.ArticleViewRepository;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.MDC;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

  private final ArticleRepository articleRepository;
  private final ArticleViewRepository articleViewRepository;
  private final CommentRepository commentRepository;
  private final S3BackupService s3BackupService;
  private final InterestRepository interestRepository;
  private final ObjectMapper objectMapper;
  private final AuditLogger auditLogger;

  @Override
  public CursorPageResponseArticleDto getArticles(
      String keyword,
      List<String> interests,
      List<String> sources,
      Instant publishDateFrom,
      Instant publishDateTo,
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

    if (publishDateFrom != null && publishDateTo != null && publishDateFrom.isAfter(publishDateTo)) {
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
        publishDateFrom,
        publishDateTo,
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

  @Override
  @Transactional
  public void softDeleteArticle(UUID articleId, UUID userId) {
    String requestId = MDC.get("requestId");
    try {
      Article article = articleRepository.findById(articleId)
          .orElseThrow(() -> new ArticleNotFoundException(articleId));
      articleRepository.delete(article);
      auditLogger.logSoftDelete(articleId, userId, requestId);
    } catch (ArticleNotFoundException e) {
      auditLogger.logSoftDeleteFailure(articleId, userId, requestId, e.getMessage());
      throw e;
    }
  }

  @Override
  @Transactional(isolation = Isolation.SERIALIZABLE)
  public void hardDeleteArticle(UUID articleId, UUID userId) {
    String requestId = MDC.get("requestId");
    try {
      Article article = articleRepository.findIncludingDeleted(articleId)
          .orElseThrow(() -> new ArticleNotFoundException(articleId));

      // Backup handled by scheduled batch job (ArticleBackupScheduler)

      int commentsDeleted = commentRepository.countByArticleId(articleId);
      int viewsDeleted = articleViewRepository.countByArticleId(articleId);

      commentRepository.deleteCommentLikesByArticleId(articleId);
      commentRepository.deleteByArticleId(articleId);
      articleViewRepository.deleteByArticleId(articleId);
      articleRepository.hardDelete(articleId);

      auditLogger.logHardDelete(articleId, userId, requestId, commentsDeleted, viewsDeleted);
    } catch (ArticleNotFoundException e) {
      auditLogger.logHardDeleteFailure(articleId, userId, requestId, e.getMessage());
      throw e;
    }
  }

  @Override
  @Transactional
  public ArticleRestoreResultDto restoreArticlesFromBackup(Instant fromDate, Instant toDate, UUID userId) {
    String requestId = MDC.get("requestId");
    if (fromDate == null || toDate == null) {
      throw new IllegalArgumentException("시작 날짜와 종료 날짜는 필수입니다");
    }

    if (fromDate.isAfter(toDate)) {
      throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다");
    }

    Duration duration = Duration.between(fromDate, toDate);
    if (duration.toDays() > 31) {
      throw new IllegalArgumentException("날짜 범위는 최대 31일까지만 가능합니다");
    }

    List<UUID> restoredArticleIds = new ArrayList<>();
    LocalDate currentDate = fromDate.atZone(ZoneOffset.UTC).toLocalDate();
    LocalDate endDate = toDate.atZone(ZoneOffset.UTC).toLocalDate();

    while (!currentDate.isAfter(endDate)) {
      try {
        List<Map<String, Object>> backupDataList = findBackupsForDate(currentDate);
        List<Map<String, Object>> missingArticles = findMissingArticles(backupDataList);
        
        for (Map<String, Object> backupData : missingArticles) {
          Article restored = restoreArticle(backupData);
          restoredArticleIds.add(restored.getId());
        }
      } catch (Exception e) {
        log.warn("날짜 {}의 백업 복원 중 오류 발생: {}", currentDate, e.getMessage());
      }
      
      currentDate = currentDate.plusDays(1);
    }

    log.info("복원 완료: {} 개의 기사 복원됨", restoredArticleIds.size());
    auditLogger.logRestore(fromDate, toDate, restoredArticleIds.size(), userId, requestId);
    return new ArticleRestoreResultDto(Instant.now(), restoredArticleIds, restoredArticleIds.size());
  }

  private List<Map<String, Object>> findBackupsForDate(LocalDate date) {
    try {
      List<ArticleBackupDto> backupDtos = s3BackupService.downloadBackup(date);
      
      List<Map<String, Object>> backups = new ArrayList<>();
      for (ArticleBackupDto dto : backupDtos) {
        Map<String, Object> backupMap = objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {});
        backups.add(backupMap);
      }
      
      log.debug("Found {} backups for date: {}", backups.size(), date);
      return backups;
    } catch (Exception e) {
      log.warn("백업 검색 실패 for date {}: {}", date, e.getMessage());
      return new ArrayList<>();
    }
  }

  private List<Map<String, Object>> findMissingArticles(List<Map<String, Object>> backupDataList) {
    if (backupDataList.isEmpty()) {
      return List.of();
    }

    Set<String> existingUrls = articleRepository.findAllSourceUrls();
    List<Map<String, Object>> missingArticles = new ArrayList<>();

    for (Map<String, Object> backupData : backupDataList) {
      String sourceUrl = (String) backupData.get("sourceUrl");
      if (sourceUrl != null && !existingUrls.contains(sourceUrl)) {
        missingArticles.add(backupData);
      }
    }

    return missingArticles;
  }

  private Article restoreArticle(Map<String, Object> backupData) {
    try {
      UUID articleId = UUID.fromString((String) backupData.get("id"));
      UUID interestId = UUID.fromString((String) backupData.get("interestId"));
      String source = (String) backupData.get("source");
      String sourceUrl = (String) backupData.get("sourceUrl");
      String title = (String) backupData.get("title");
      String summary = (String) backupData.get("summary");
      
      Instant publishDate = parseInstant(backupData.get("publishDate"));
      Instant createdAt = parseInstant(backupData.get("createdAt"));
      
      Number viewCountNum = (Number) backupData.get("viewCount");
      Number commentCountNum = (Number) backupData.get("commentCount");
      
      long viewCount = viewCountNum != null ? viewCountNum.longValue() : 0L;
      long commentCount = commentCountNum != null ? commentCountNum.longValue() : 0L;

      Interest interest = interestRepository.findById(interestId)
          .orElseGet(() -> getDefaultInterest());

      Article article = Article.restore(
          articleId,
          interest,
          ArticleSource.valueOf(source),
          sourceUrl,
          title,
          publishDate,
          summary,
          viewCount,
          commentCount,
          createdAt
      );

      Article saved = articleRepository.save(article);
      log.info("기사 복원 완료: articleId={}, sourceUrl={}", articleId, sourceUrl);
      
      return saved;
    } catch (Exception e) {
      log.error("기사 복원 실패: {}", backupData, e);
      throw new RuntimeException("기사 복원 실패", e);
    }
  }

  private Instant parseInstant(Object value) {
    if (value == null) {
      return Instant.now();
    }
    if (value instanceof String) {
      return Instant.parse((String) value);
    }
    if (value instanceof Number) {
      return Instant.ofEpochMilli(((Number) value).longValue());
    }
    return Instant.now();
  }

  private Interest getDefaultInterest() {
    return interestRepository.findAll().stream()
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("최소 하나의 Interest가 존재해야 합니다"));
  }
}
