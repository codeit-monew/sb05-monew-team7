package com.spring.monew.commentlike.service;

import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import java.util.UUID;

public interface CommentLikeService {
  CommentLikeDto addCommentLike(UUID commentId, UUID userId);

  void removeCommentLike(UUID commentId, UUID userId);
}
