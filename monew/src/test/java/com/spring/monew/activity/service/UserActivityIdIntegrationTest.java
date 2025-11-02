package com.spring.monew.activity.service;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.*;
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
class UserActivityIdIntegrationTest {

  private static final String HEADER_USER = "Monew-Request-User-ID";

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;
  @Autowired private InterestRepository interestRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private CommentRepository commentRepository;

  @Autowired private EntityManager em;

  private User owner;   // 경로 {userId}
  private User other;   // 타 사용자
  private UUID ownerId;

  @BeforeEach
  void setup() {
    owner = userRepository.save(new User("owner@test.com", "owner-nick", "pw"));
    other  = userRepository.save(new User("other@test.com", "other-nick", "pw"));
    ownerId = owner.getId();

    Interest interest = interestRepository.save(new Interest("tech", List.of("spring", "java")));
    Article article = articleRepository.save(
        Article.of(interest, ArticleSource.NAVER, "https://ex.com/a1", "제목1", Instant.now(), "요약1")
    );
    commentRepository.save(new Comment(article, owner, "소유자 댓글 1"));

    em.flush();
    em.clear();
  }

  @Test
  @Order(1)
  @DisplayName("헤더 없으면 200 (공개 조회 허용) + 기본 구조")
  void public_access_ok_without_header() throws Exception {
    mockMvc.perform(get("/api/user-activities/{userId}", ownerId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ownerId.toString()))
        .andExpect(jsonPath("$.email", notNullValue()))
        .andExpect(jsonPath("$.nickname", notNullValue()))
        .andExpect(jsonPath("$.subscriptions").isArray())
        .andExpect(jsonPath("$.comments").isArray())
        .andExpect(jsonPath("$.commentLikes").isArray())
        .andExpect(jsonPath("$.articleViews").isArray());
  }

  @Test
  @Order(2)
  @DisplayName("소유자 접근: 200 + 기본 구조(최상위 필드)")
  void owner_access_ok() throws Exception {
    mockMvc.perform(get("/api/user-activities/{userId}", ownerId)
            .header(HEADER_USER, ownerId.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(ownerId.toString()))
        .andExpect(jsonPath("$.email", notNullValue()))
        .andExpect(jsonPath("$.nickname", notNullValue()))
        .andExpect(jsonPath("$.subscriptions").isArray())
        .andExpect(jsonPath("$.comments").isArray())
        .andExpect(jsonPath("$.commentLikes").isArray())
        .andExpect(jsonPath("$.articleViews").isArray());
  }

  @Test
  @Order(3)
  @DisplayName("타인 접근: 403")
  void other_access_forbidden() throws Exception {
    mockMvc.perform(get("/api/user-activities/{userId}", ownerId)
            .header(HEADER_USER, other.getId().toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @Order(4)
  @DisplayName("존재하지 않는 사용자 헤더로 접근: 403")
  void random_header_user_forbidden() throws Exception {
    UUID random = UUID.randomUUID();
    mockMvc.perform(get("/api/user-activities/{userId}", ownerId)
            .header(HEADER_USER, random.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }
}
