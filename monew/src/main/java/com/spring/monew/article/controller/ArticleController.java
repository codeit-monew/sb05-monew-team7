package com.spring.monew.article.controller;

import com.spring.monew.article.controller.dto.response.ArticleDto;
import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import com.spring.monew.article.service.ArticleService;
import com.spring.monew.auth.config.HeaderUserAuthentication;
import com.spring.monew.common.util.RequestUserExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Validated
@Tag(name = "기사", description = "기사 목록 조회 및 검색 API")
public class ArticleController {

  private final ArticleService articleService;
  private final RequestUserExtractor userExtractor;

  @GetMapping
  @Operation(summary = "기사 목록 조회", description = "필터, 검색, 정렬 기능을 지원하는 페이지네이션 기사 목록 조회")
  public CursorPageResponseArticleDto articleList(
      @Parameter(description = "검색어(제목, 요약)")
      @RequestParam(required = false) String keyword,

      @Parameter(description = "관심사 ID")
      @RequestParam(required = false) List<String> interests,

      @Parameter(description = "출처(포함)")
      @RequestParam(name = "sourceIn", required = false) List<String> sources,

      @Parameter(description = "날짜 시작(범위)")
      @RequestParam(required = false)
      Instant publishDateFrom,

      @Parameter(description = "날짜 끝(범위)")
      @RequestParam(required = false)
      Instant publishDateTo,

      @Parameter(description = "정렬 속성 이름", required = true)
      @RequestParam(required = true) String orderBy,

      @Parameter(description = "정렬 방향 (ASC, DESC)", required = true)
      @RequestParam(required = true) String direction,

      @Parameter(description = "커서 값")
      @RequestParam(required = false) String cursor,

      @Parameter(description = "커서 페이지 크기", required = true)
      @RequestParam(required = true) 
      @Min(1) 
      @Max(100) 
      int limit,

      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);

    return articleService.getArticles(
        keyword,
        interests,
        sources,
        publishDateFrom,
        publishDateTo,
        orderBy,
        direction,
        cursor,
        limit,
        userId
    );
  }

  @GetMapping("/{articleId}")
  @Operation(
      summary = "뉴스 기사 단건 조회",
      description = "뉴스 기사 ID를 사용하여 특정 기사의 상세 정보를 조회합니다. 조회 시 자동으로 조회수가 증가하며, 같은 사용자의 중복 조회는 24시간 동안 1회만 카운트됩니다."
  )
  public ArticleDto articleDetails(
      @Parameter(description = "뉴스 기사 ID", required = true)
      @PathVariable UUID articleId,
      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);

    return articleService.getArticle(articleId, userId);
  }

  @GetMapping("/sources")
  @Operation(summary = "기사 출처 목록 조회", description = "뉴스 기사 출처 enum 값 목록 반환")
  public List<String> articleSourceList() {
    return articleService.getSources();
  }
}
