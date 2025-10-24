package com.spring.monew.commentlike.service;

import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;

public interface CommentLikeService {
  CommentLikeDto addCommentLike();

  void removeCommentLike();
}
