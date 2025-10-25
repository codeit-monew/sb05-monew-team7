package com.spring.monew.commentlike.controller;

import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.service.CommentLikeService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentLikeController {
  private final CommentLikeService commentLikeService;

  @PostMapping("/{commentId}/comment-likes")
  public CommentLikeDto commentLikeAdd(@PathVariable UUID commentId){
    return null;
  }

  @DeleteMapping("/{commentId}/comment-likes")
  public void commentLikeDelete(@PathVariable UUID commentId){

  }
}
