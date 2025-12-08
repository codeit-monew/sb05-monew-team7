package com.spring.monew.commentlike.service;

import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.domain.CommentLike;
import java.util.UUID;

public interface CommentLikeService {
  CommentLike addCommentLike(UUID commentId, UUID userId);

  CommentLike removeCommentLike(UUID commentId, UUID userId);
}
