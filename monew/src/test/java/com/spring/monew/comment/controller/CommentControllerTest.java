package com.spring.monew.comment.controller;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.request.CommentUpdateRequest;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.service.CommentService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false) // Security Filter 비활성화
class CommentControllerTest {

  @Autowired
  MockMvc mvc;

  @Autowired
  ObjectMapper mapper;

  @MockitoBean
  CommentService commentService;

  UUID userId = UUID.randomUUID();
  UUID commentId = UUID.randomUUID();
  UUID articleId = UUID.randomUUID();

  @Test
  @DisplayName("댓글 등록 성공")
  void commentAdd() throws Exception {
    CommentRegisterRequest req = new CommentRegisterRequest(articleId, userId, "내용");

    CommentDto res = new CommentDto(
        commentId, articleId, userId, "닉네임", "내용", 0,
        false, Instant.now()
    );

    when(commentService.addComment(any())).thenReturn(res);

    mvc.perform(post("/api/comments")
            .header("Monew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", is("내용")));

    verify(commentService).addComment(any());
  }

  @Test
  @DisplayName("댓글 수정 성공")
  void commentModify() throws Exception {
    CommentUpdateRequest req = new CommentUpdateRequest("수정내용");

    CommentDto res = new CommentDto(
        commentId, articleId, userId, "닉네임", "수정내용", 0,
        false, Instant.now()
    );

    when(commentService.modifyComment(eq(commentId), eq(userId), any())).thenReturn(res);

    mvc.perform(patch("/api/comments/{id}", commentId)
            .header("Monew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", is("수정내용")));

    verify(commentService).modifyComment(eq(commentId), eq(userId), any());
  }

  @Test
  @DisplayName("댓글 논리 삭제 성공")
  void commentDeleteLogical() throws Exception {

    mvc.perform(delete("/api/comments/{id}", commentId)
            .header("Monew-Request-User-ID", userId.toString()))
        .andExpect(status().isOk());

    verify(commentService).removeCommentLogical(commentId);
  }

  @Test
  @DisplayName("댓글 물리 삭제 성공")
  void commentDeleteHard() throws Exception {

    mvc.perform(delete("/api/comments/{id}/hard", commentId)
            .header("Monew-Request-User-ID", userId.toString()))
        .andExpect(status().isOk());

    verify(commentService).removeCommentHard(commentId);
  }
}