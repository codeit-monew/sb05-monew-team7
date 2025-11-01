package com.spring.monew.commentlike.controller;

import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.commentlike.service.CommentLikeService;
import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.service.NotificationService;
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
  private final UserActivityService userActivityService;
  private final NotificationService notificationService;

  @PostMapping("/{commentId}/comment-likes")
  public CommentLikeDto commentLikeAdd(@PathVariable UUID commentId,
    Principal principal){
    UUID userId = userExtractor.extractUserId(principal);
    CommentLike commentLike = commentLikeService.addCommentLike(commentId, userId);

    userActivityService.addCommentLikeActivity(commentLike);

    notificationService.create(userId, commentLike.getUser().getNickname(), NotificationResourceType.COMMENT, commentLike.getComment().getId());

    return new CommentLikeDto(
        commentLike.getId(),
        commentLike.getUser().getId(),
        commentLike.getCreatedAt(),
        commentLike.getComment().getId(),
        commentLike.getComment().getArticle().getId(),
        commentLike.getUser().getId(),
        commentLike.getUser().getNickname(),
        commentLike.getComment().getContent(),
        commentLike.getComment().getLikeCount(),
        commentLike.getComment().getCreatedAt()
    );
  }

  @DeleteMapping("/{commentId}/comment-likes")
  public void commentLikeDelete(@PathVariable UUID commentId,
      Principal principal){
    UUID userId = userExtractor.extractUserId(principal);
    CommentLike commentLike = commentLikeService.removeCommentLike(commentId, userId);

    userActivityService.removeCommentLikeActivity(commentLike.getId());
  }
}
