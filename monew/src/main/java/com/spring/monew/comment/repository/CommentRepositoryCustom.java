package com.spring.monew.comment.repository;

import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import java.time.Instant;
import java.util.UUID;

public interface CommentRepositoryCustom{

  CursorPageResponseCommentDto findCursorPagedComments(
      UUID articleId,
      String orderBy,
      String direction,
      String cursor,
      Instant after,
      int limit,
      UUID userId
  );
}