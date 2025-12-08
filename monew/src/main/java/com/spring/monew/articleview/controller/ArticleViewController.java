package com.spring.monew.articleview.controller;

import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import com.spring.monew.articleview.service.ArticleViewService;
import com.spring.monew.common.util.RequestUserExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "뉴스 기사 조회 추적", description = "기사 조회수 추적 API")
public class ArticleViewController {

    private final ArticleViewService articleViewService;
    private final RequestUserExtractor userExtractor;

    @PostMapping("/{articleId}/article-views")
    @Operation(
        summary = "기사 조회 추적",
        description = "사용자의 기사 조회를 명시적으로 추적합니다. 24시간 이내 중복 조회는 무시되며, 항상 200 OK를 반환합니다 (멱등성)."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 추적 성공 (신규 조회 또는 중복 조회 모두 포함)"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 (X-User-Id 헤더 누락)"),
        @ApiResponse(responseCode = "404", description = "존재하지 않는 기사"),
        @ApiResponse(responseCode = "500", description = "서버 오류 (Redis 연결 실패 등)")
    })
    public ResponseEntity<ArticleViewDto> trackArticleView(
        @Parameter(description = "기사 ID", required = true)
        @PathVariable UUID articleId,
        Principal principal
    ) {
        UUID userId = userExtractor.extractUserId(principal);

        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        ArticleViewDto result = articleViewService.trackView(articleId, userId);
        return ResponseEntity.ok(result);
    }
}
