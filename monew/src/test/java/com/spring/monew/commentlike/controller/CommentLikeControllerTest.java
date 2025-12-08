package com.spring.monew.commentlike.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.commentlike.service.CommentLikeService;
import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.config.TestSecurityConfig;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.notification.service.NotificationService;
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

@WebMvcTest(CommentLikeController.class)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
class CommentLikeControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private CommentLikeService commentLikeService;
  @MockitoBean private RequestUserExtractor userExtractor;
  @MockitoBean private UserActivityService userActivityService;
  @MockitoBean private NotificationService notificationService;

  @Test
  @DisplayName("댓글 좋아요 추가 성공")
  void addCommentLike_success() throws Exception {
    // given
    UUID commentLikeId = UUID.randomUUID();
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    User user = new User(userId,"email@gmail.com", "user", "password");
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


    // ✅ @TestOnly 생성자로 CommentLike 생성
    CommentLike commentLike = new CommentLike(
        commentLikeId,
        comment,
        user,
        Instant.now()
    );

    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(commentLikeService.addCommentLike(commentId, userId)).willReturn(commentLike);

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
  void removeCommentLike_success() throws Exception {
    // given
    UUID commentId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    UUID commentLikeId = UUID.randomUUID();

    // 테스트용 유저, 댓글, 엔티티 구성
    User user = new User(userId, "email@gmail.com", "user", "password");

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
        commentId,
        user,
        article,
        "테스트 댓글",
        false,
        0,
        Instant.now()
    );

    CommentLike commentLike = new CommentLike(
        commentLikeId,
        comment,
        user,
        Instant.now()
    );

    given(userExtractor.extractUserId(any())).willReturn(userId);
    given(commentLikeService.removeCommentLike(commentId, userId)).willReturn(commentLike); // ✅ 변경 포인트

    // when & then
    mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
            .header("Monew-Request-User-ID", userId))
        .andExpect(status().isOk());
  }

}
