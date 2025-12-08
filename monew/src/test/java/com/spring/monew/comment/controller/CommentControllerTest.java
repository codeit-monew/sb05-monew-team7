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
import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.request.CommentUpdateRequest;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.service.CommentService;
import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.config.TestSecurityConfig;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.user.domain.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc
class CommentControllerTest {

  @Autowired
  MockMvc mvc;
  @Autowired
  ObjectMapper mapper;

  @MockitoBean
  CommentService commentService;
  @MockitoBean
  RequestUserExtractor userExtractor;
  @MockitoBean
  UserActivityService userActivityService;

  UUID userId = UUID.randomUUID();
  UUID commentId = UUID.randomUUID();
  UUID articleId = UUID.randomUUID();

  @Test
  @DisplayName("댓글 등록 성공")
  void commentAdd_success() throws Exception {
    // given
    CommentRegisterRequest req = new CommentRegisterRequest(articleId, userId, "내용");

    User user = new User("email@gmail.com", "user", "password");
    Article article = new Article(
        UUID.randomUUID(),
        new Interest("관심사 테스트", List.of()),
        ArticleSource.CHOSUN,
        "https://test.com/news/123",
        "테스트 기사",
        Instant.now(),
        "이것은 테스트용 기사 요약",
        3L,        // commentCount
        50L,       // viewCount
        false,     // isDeleted
        Instant.now(),
        Instant.now()
    );

    Comment comment = new Comment(
        commentId, user, article,
        "내용", false, 0, Instant.now()
    );

    when(commentService.addComment(any())).thenReturn(comment);

    // when & then
    mvc.perform(post("/api/comments")
            .header("Monew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", is("내용")))
        .andExpect(jsonPath("$.userNickname", is("user")));

    verify(commentService).addComment(any());
    verify(userActivityService).addCommentActivity(comment);
  }

  @Test
  @DisplayName("댓글 수정 성공")
  void commentModify_success() throws Exception {
    // given
    CommentUpdateRequest req = new CommentUpdateRequest("수정내용");

    User user = new User("email@gmail.com", "user", "password");
    Article article = new Article(
        UUID.randomUUID(),
        new Interest("관심사 테스트", List.of()),
        ArticleSource.CHOSUN,
        "https://test.com/news/123",
        "테스트 기사",
        Instant.now(),
        "이것은 테스트용 기사 요약",
        3L,        // commentCount
        50L,       // viewCount
        false,     // isDeleted
        Instant.now(),
        Instant.now()
    );

    Comment comment = new Comment(
        commentId, user, article,
        "수정내용", false, 0, Instant.now()
    );

    when(userExtractor.extractUserId(any())).thenReturn(userId);
    when(commentService.modifyComment(eq(commentId), eq(userId), any())).thenReturn(comment);

    // when & then
    mvc.perform(patch("/api/comments/{commentId}", commentId)
            .header("Monew-Request-User-ID", userId.toString())
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", is("수정내용")));

    verify(commentService).modifyComment(eq(commentId), eq(userId), any());
    verify(userActivityService).addCommentActivity(comment);
  }

  @Test
  @DisplayName("댓글 논리 삭제 성공")
  void commentDeleteLogical_success() throws Exception {
    mvc.perform(delete("/api/comments/{commentId}", commentId)
            .header("Monew-Request-User-ID", userId.toString()))
        .andExpect(status().isOk());

    verify(commentService).removeCommentLogical(commentId);
    verify(userActivityService).removeCommentActivity(commentId);
  }

  @Test
  @DisplayName("댓글 물리 삭제 성공")
  void commentDeleteHard_success() throws Exception {
    mvc.perform(delete("/api/comments/{commentId}/hard", commentId)
            .header("Monew-Request-User-ID", userId.toString()))
        .andExpect(status().isOk());

    verify(commentService).removeCommentHard(commentId);
    verify(userActivityService).removeCommentActivity(commentId);
  }
}
