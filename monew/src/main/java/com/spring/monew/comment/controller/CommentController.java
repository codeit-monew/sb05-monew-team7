package com.spring.monew.comment.controller;

import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.request.CommentUpdateRequest;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.service.CommentService;
import com.spring.monew.common.util.RequestUserExtractor;
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
public class CommentController {

  private final CommentService commentService;
  private final RequestUserExtractor userExtractor;
  private final UserActivityService userActivityService;

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

  @DeleteMapping("/{commentId}")
  public void commentDeleteLogical(@PathVariable UUID commentId) {
    commentService.removeCommentLogical(commentId);
    userActivityService.removeCommentActivity(commentId);
  }

  @DeleteMapping("/{commentId}/hard")
  public void commentDeleteHard(@PathVariable UUID commentId) {
    commentService.removeCommentHard(commentId);
    userActivityService.removeCommentActivity(commentId);
  }
}
