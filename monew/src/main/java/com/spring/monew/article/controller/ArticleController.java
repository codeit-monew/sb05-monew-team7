package com.spring.monew.article.controller;

import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import com.spring.monew.article.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "기사", description = "기사 목록 조회 및 검색 API")
public class ArticleController {

  private final ArticleService articleService;

  @GetMapping
  @Operation(summary = "기사 목록 조회", description = "필터, 검색, 정렬 기능을 지원하는 페이지네이션 기사 목록 조회")
  public CursorPageResponseArticleDto articleList(
      @Parameter(description = "제목/요약 검색 키워드")
      @RequestParam(required = false) String keyword,

      @Parameter(description = "관심사 이름으로 필터링")
      @RequestParam(required = false) List<String> interests,

      @Parameter(description = "기사 출처로 필터링")
      @RequestParam(required = false) List<String> sources,

      @Parameter(description = "이 날짜 이후 발행된 기사 필터링")
      @RequestParam(required = false) Instant from,

      @Parameter(description = "이 날짜 이전 발행된 기사 필터링")
      @RequestParam(required = false) Instant to,

      @Parameter(description = "정렬 기준 필드 (publishDate, viewCount, commentCount, createdAt)")
      @RequestParam(defaultValue = "createdAt") String orderBy,

      @Parameter(description = "정렬 방향 (ASC 또는 DESC)")
      @RequestParam(defaultValue = "DESC") String direction,

      @Parameter(description = "페이지네이션 커서")
      @RequestParam(required = false) String cursor,

      @Parameter(description = "페이지당 항목 수 (1-100)")
      @RequestParam(defaultValue = "20") int limit,

      @Parameter(description = "조회 여부 계산을 위한 사용자 ID")
      @RequestHeader(name = "Monew-Request-User-ID", required = false) UUID userId
  ) {
    return articleService.getArticles(
        keyword,
        interests,
        sources,
        from,
        to,
        orderBy,
        direction,
        cursor,
        limit,
        userId
    );
  }
}
