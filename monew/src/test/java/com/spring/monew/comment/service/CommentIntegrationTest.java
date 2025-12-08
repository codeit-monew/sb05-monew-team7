package com.spring.monew.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.request.CommentUpdateRequest;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
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
class CommentIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ArticleRepository articleRepository;
  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private CommentRepository commentRepository;

  private User user;
  private Article article;

  @BeforeEach
  void setup() {
    user = userRepository.save(new User("email@test.com", "nick", "password"));
    Interest interest = interestRepository.save(new Interest("it", List.of("dev", "code")));
    article = articleRepository.save(
        Article.of(interest, ArticleSource.CHOSUN, "http://dummy.com", "title", Instant.now(),
            "summary")
    );
  }

  @Test
  @DisplayName("댓글 등록 성공")
  void addComment() throws Exception {
    CommentRegisterRequest request =
        new CommentRegisterRequest(article.getId(), user.getId(), "댓글 내용");

    mockMvc.perform(post("/api/comments")
            .header("Monew-Request-User-ID", user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("댓글 내용"));
  }


  @Test
  @DisplayName("댓글 목록 조회")
  void getComments() throws Exception {
    // given
    commentRepository.save(new Comment(article, user, "조회용 댓글"));

    // when & then
    mockMvc.perform(get("/api/comments")
            .param("articleId", article.getId().toString())
            .header("Monew-Request-User-ID", user.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].content").value("조회용 댓글"));
  }

  @Test
  @DisplayName("댓글 수정 성공")
  void modifyComment() throws Exception {
    Comment saved = commentRepository.save(new Comment(article, user, "old"));

    CommentUpdateRequest updateRequest = new CommentUpdateRequest("new content");

    mockMvc.perform(patch("/api/comments/{commentId}", saved.getId())
            .header("Monew-Request-User-ID", user.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").value("new content"));
  }

  @Test
  @DisplayName("댓글 논리 삭제 성공")
  void removeCommentLogical() throws Exception {
    Comment saved = commentRepository.save(new Comment(article, user, "삭제할 댓글"));

    mockMvc.perform(delete("/api/comments/{commentId}", saved.getId()))
        .andExpect(status().isOk());

    // 확인
    Comment deleted = commentRepository.findIncludingDeleted(saved.getId())
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 댓글입니다."));

    assertThat(deleted.isDeleted()).isTrue();

  }

  @Test
  @DisplayName("댓글 물리 삭제 성공")
  void removeCommentHard() throws Exception {
    Comment saved = commentRepository.save(new Comment(article, user, "하드삭제 대상"));

    mockMvc.perform(delete("/api/comments/{commentId}/hard", saved.getId()))
        .andExpect(status().isOk());

    assertThat(commentRepository.findById(saved.getId()).isPresent()).isFalse();
  }
}