package com.spring.monew.comment.controller;

import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.request.CommentUpdateRequest;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.service.CommentService;
import com.spring.monew.common.util.RequestUserExtractor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "댓글 관리", description = "댓글 관련 API")
public class CommentController {

  private final CommentService commentService;
  private final RequestUserExtractor userExtractor;
  private final UserActivityService userActivityService;

  @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping
  public CommentDto commentAdd(
      @RequestBody CommentRegisterRequest registerRequest) {
    Comment comment = commentService.addComment(registerRequest);
    userActivityService.addCommentActivity(comment);
    return new CommentDto(
        comment.getId(),
        comment.getArticle().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount(),
        false,
        comment.getCreatedAt()
    );
  }

  @Operation(summary = "댓글 목록 조회", description = "조건에 맞는 댓글 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public CursorPageResponseCommentDto commentList(
      @RequestParam UUID articleId,
      @RequestParam(defaultValue = "name") String orderBy,          // 정렬 기준 (기본값 name)
      @RequestParam(defaultValue = "ASC") String direction,         // 정렬 방향 (기본값 ASC)
      @RequestParam(required = false) String cursor,                // 커서 값
      @RequestParam(required = false) Instant after,                // 보조 커서(createdAt)
      @RequestParam(defaultValue = "50") int limit,                 // 페이지 크기
      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);
    return commentService.getComments(articleId, orderBy,
        direction, cursor, after, limit, userId);
  }

  @Operation(summary = "댓글 정보 수정", description = "댓글의 내용을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{commentId}")
  public CommentDto commentModify(@PathVariable UUID commentId,
      Principal principal,
      @RequestBody CommentUpdateRequest updateRequest) {
    UUID userId = userExtractor.extractUserId(principal);
    Comment comment = commentService.modifyComment(commentId, userId, updateRequest);

    userActivityService.addCommentActivity(comment);

    return new CommentDto(
        comment.getId(),
        comment.getArticle().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount(),
        false,
        comment.getCreatedAt()
    );
  }

  @Operation(summary = "댓글 논리 삭제", description = "댓글을 논리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{commentId}")
  public void commentDeleteLogical(@PathVariable UUID commentId) {
    commentService.removeCommentLogical(commentId);
    userActivityService.removeCommentActivity(commentId);
  }

  @Operation(summary = "댓글 물리 삭제", description = "댓글을 물리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "404", description = "댓글 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{commentId}/hard")
  public void commentDeleteHard(@PathVariable UUID commentId) {
    commentService.removeCommentHard(commentId);
    userActivityService.removeCommentActivity(commentId);
  }
}
