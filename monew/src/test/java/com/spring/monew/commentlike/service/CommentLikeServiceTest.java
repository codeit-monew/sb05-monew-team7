package com.spring.monew.commentlike.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.commentlike.repository.CommentLikeRepository;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class CommentLikeIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;
  @Autowired private InterestRepository interestRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private CommentRepository commentRepository;
  @Autowired private CommentLikeRepository commentLikeRepository;

  private User user;
  private Comment comment;

  @BeforeEach
  void setup() {
    user = userRepository.save(new User("email@test.com", "nick", "password"));
    Interest interest = interestRepository.save(new Interest("tech", List.of("java", "spring")));
    Article article = articleRepository.save(
        Article.of(interest, ArticleSource.CHOSUN, "https://dummy.com", "title", Instant.now(), "summary")
    );
    comment = commentRepository.save(new Comment(article, user, "좋아요 테스트 댓글"));
  }

  @Test
  @DisplayName("댓글 좋아요 추가 성공")
  void addCommentLike() throws Exception {
    mockMvc.perform(post("/api/comments/{commentId}/comment-likes", comment.getId())
            .header("Monew-Request-User-ID", user.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.commentId").value(comment.getId().toString()))
        .andExpect(jsonPath("$.likedBy").value(user.getId().toString()));

    assertThat(commentLikeRepository.existsByComment_IdAndUser_Id(comment.getId(), user.getId()))
        .isTrue();
  }

  @Test
  @DisplayName("댓글 좋아요 취소 성공")
  void removeCommentLike() throws Exception {
    CommentLike like = commentLikeRepository.save(new CommentLike(comment, user));

    mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", comment.getId())
            .header("Monew-Request-User-ID", user.getId()))
        .andExpect(status().isOk());

    assertThat(commentLikeRepository.findById(like.getId())).isEmpty();
  }

  @Test
  @DisplayName("중복 좋아요 시 400 에러 반환")
  void duplicateLikeThrowsError() throws Exception {
    commentLikeRepository.save(new CommentLike(comment, user));

    mockMvc.perform(post("/api/comments/{commentId}/comment-likes", comment.getId())
            .header("Monew-Request-User-ID", user.getId())
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().is4xxClientError());
  }
}
