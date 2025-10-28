package com.spring.monew.comment.service;

import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.request.CommentUpdateRequest;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import java.time.Instant;
import java.util.UUID;

public interface CommentService {

  CommentDto addComment(CommentRegisterRequest registerRequest);

  CursorPageResponseCommentDto getComments(UUID articleId, String orderBy,
      String direction, String cursor, Instant after, int limit, UUID userId);

  CommentDto modifyComment(UUID commentId, UUID userId, CommentUpdateRequest updateRequest);

  void removeCommentLogical(UUID commentId);

  void removeCommentHard(UUID commentId);
}
