package com.spring.monew.comment.controller;

import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import com.spring.monew.comment.service.CommentService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
  private final CommentService commentService;

  @PostMapping
  public CommentDto commentAdd(CommentRegisterRequest registerRequest){
    return null;
  }

  @GetMapping
  public CursorPageResponseCommentDto commentList(){
    return null;
  }

  @PatchMapping("/{commentId}")
  public CommentDto commentModify(@PathVariable UUID commentId){
    return null;
  }

  @DeleteMapping("/{commentId}")
  public void commentDeleteLogical(@PathVariable UUID commentId){

  }

  @DeleteMapping("/{commentId}/hard")
  public void commentDeleteHard(@PathVariable UUID commentId){

  }
}
