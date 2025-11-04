package com.spring.monew.article.controller;

import com.spring.monew.article.controller.dto.response.ArticleDto;
import com.spring.monew.article.controller.dto.response.ArticleRestoreResultDto;
import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import com.spring.monew.article.service.ArticleService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Validated
@Tag(name = "뉴스 기사 관리", description = "기사 목록 조회 및 검색 API")
public class ArticleController {

  private final ArticleService articleService;
  private final RequestUserExtractor userExtractor;

  @GetMapping
  @Operation(summary = "뉴스 기사 목록 조회", description = "조건에 맞는 뉴스 기사 목록을 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)"),
          @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
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
          description = "뉴스 기사 ID로 뉴스 기사 단건을 조회합니다."
  )
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "404", description = "뉴스 기사 정보 없음"),
          @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  public ArticleDto articleDetails(
      @Parameter(description = "뉴스 기사 ID", required = true)
      @PathVariable UUID articleId,
      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);

    return articleService.getArticle(articleId, userId);
  }

  @GetMapping("/sources")
  @Operation(summary = "출처 목록 조회", description = "출처 목록을 조회합니다.")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "조회 성공"),
          @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  public List<String> articleSourceList() {
    return articleService.getSources();
  }

  @DeleteMapping("/{articleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "뉴스 기사 삭제", description = "특정 기사를 소프트 삭제합니다 (논리 삭제)")
  public void softDeleteArticle(
      @Parameter(description = "뉴스 기사 ID", required = true)
      @PathVariable UUID articleId,
      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);
    articleService.softDeleteArticle(articleId, userId);
  }

  @DeleteMapping("/{articleId}/hard")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "뉴스 기사 영구 삭제", description = "특정 기사 및 관련된 모든 데이터(댓글, 좋아요, 조회수)를 데이터베이스에서 완전히 삭제합니다")
  public void hardDeleteArticle(
      @Parameter(description = "뉴스 기사 ID", required = true)
      @PathVariable UUID articleId,
      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);
    articleService.hardDeleteArticle(articleId, userId);
  }

  @GetMapping("/restore")
  @Operation(
      summary = "백업에서 기사 복원",
      description = "S3 백업에서 지정된 날짜 범위의 누락된 기사를 복원합니다. 최대 31일 범위까지 가능합니다."
  )
  public ResponseEntity<ArticleRestoreResultDto> restoreArticles(
      @Parameter(description = "시작 날짜", required = true)
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      
      @Parameter(description = "종료 날짜", required = true)
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      
      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);
    ArticleRestoreResultDto result = articleService.restoreArticlesFromBackup(from, to, userId);
    return ResponseEntity.ok(result);
  }
}
