package com.spring.monew.comment.service.impl;

import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;

  @Override
  public CommentDto addComment() {
    return null;
  }

  @Override
  public CursorPageResponseCommentDto getComments() {
    return null;
  }

  @Override
  public CommentDto modifyComment() {
    return null;
  }

  @Override
  public void removeCommentLogical() {

  }

  @Override
  public void removeCommentHard() {

  }
}
