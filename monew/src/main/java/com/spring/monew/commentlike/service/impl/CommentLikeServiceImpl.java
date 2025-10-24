package com.spring.monew.commentlike.service.impl;

import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.repository.CommentLikeRepository;
import com.spring.monew.commentlike.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

  private final CommentLikeRepository commentLikeRepository;

  @Override
  public CommentLikeDto addCommentLike() {
    return null;
  }

  @Override
  public void removeCommentLike() {

  }
}
