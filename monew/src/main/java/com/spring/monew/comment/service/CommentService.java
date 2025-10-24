package com.spring.monew.comment.service;

import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;

public interface CommentService {

  CommentDto addComment();

  CursorPageResponseCommentDto getComments();

  CommentDto modifyComment();

  void removeCommentLogical();

  void removeCommentHard();
}
