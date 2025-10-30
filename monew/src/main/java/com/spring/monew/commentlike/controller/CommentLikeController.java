package com.spring.monew.commentlike.controller;

import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.service.CommentLikeService;
import com.spring.monew.common.util.RequestUserExtractor;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentLikeController {
  private final CommentLikeService commentLikeService;
  private final RequestUserExtractor userExtractor;

  @PostMapping("/{commentId}/comment-likes")
  public CommentLikeDto commentLikeAdd(@PathVariable UUID commentId,
    Principal principal){
    UUID userId = userExtractor.extractUserId(principal);
    return commentLikeService.addCommentLike(commentId, userId);
  }

  @DeleteMapping("/{commentId}/comment-likes")
  public void commentLikeDelete(@PathVariable UUID commentId,
      Principal principal){
    UUID userId = userExtractor.extractUserId(principal);
    commentLikeService.removeCommentLike(commentId, userId);

  }
}
