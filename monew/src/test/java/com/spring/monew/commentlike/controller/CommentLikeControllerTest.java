package com.spring.monew.commentlike.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.service.CommentLikeService;
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

@WebMvcTest(CommentLikeController.class)
@AutoConfigureMockMvc(addFilters = false) // Security 필터 비활성화
class CommentLikeControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private CommentLikeService commentLikeService;

  @Test
  @DisplayName("댓글 좋아요 추가 성공")
  void addCommentLike() throws Exception {
    // given
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID articleId = UUID.randomUUID();

    CommentLikeDto responseDto = new CommentLikeDto(
        UUID.randomUUID(), userId, Instant.now(), commentId, articleId, userId,
        "작성자 닉네임", "내용", 0, Instant.now()
    );

    given(commentLikeService.addCommentLike(commentId, userId)).willReturn(responseDto);

    // when & then
    mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
            .header("Monew-Request-User-ID", userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.commentId").value(commentId.toString()))
        .andExpect(jsonPath("$.likedBy").value(userId.toString()));
  }

  @Test
  @DisplayName("댓글 좋아요 취소 성공")
  void removeCommentLike() throws Exception {
    // given
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    willDoNothing().given(commentLikeService).removeCommentLike(commentId, userId);

    // when & then
    mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk());
  }
}
