package com.spring.monew.activity.service;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles({"test", "activity-mongo"})
@SpringBootTest
@AutoConfigureMockMvc // filters=true (기본값) → SecurityFilterChain 활성
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class UserActivityIntegrationTest {

  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Autowired UserRepository userRepository;
  @Autowired ArticleRepository articleRepository;
  @Autowired InterestRepository interestRepository;

  @Autowired MongoTemplate mongoTemplate;
  @PersistenceContext EntityManager em;

  private User user;
  private Article article;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    // JPA/H2
    r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    r.add("spring.jpa.show-sql", () -> "false");
    r.add("spring.sql.init.mode", () -> "never");
    r.add("spring.flyway.enabled", () -> "false");
    r.add("spring.liquibase.enabled", () -> "false");
    r.add("spring.batch.job.enabled", () -> "false");

    // Embedded Mongo (Flapdoodle)
    r.add("spring.mongodb.embedded.version", () -> "6.0.5");
    r.add("spring.data.mongodb.database", () -> "testdb");
  }

  @BeforeEach
  void setup() {
    // --- RDB 시드 ---
    user = userRepository.save(new User("activity@test.com", "act-nick", "password"));
    ensureCreatedAtExists(em, user.getId(), Instant.parse("2025-01-01T00:00:00Z"));

    Interest interest = interestRepository.save(new Interest("it", List.of("dev", "code")));
    article = articleRepository.save(
        Article.of(
            interest,
            ArticleSource.CHOSUN,
            "http://example.com/1",
            "title",
            Instant.parse("2025-01-01T00:00:00Z"),
            "summary"
        )
    );

    // --- Mongo 시드 ---
    // activity_article_views
    mongoTemplate.save(new Document()
            .append("_id", UUID.randomUUID().toString())
            .append("userId", user.getId())
            .append("articleId", article.getId())
            .append("source", ArticleSource.CHOSUN.name())
            .append("sourceUrl", "http://example.com/1")
            .append("title", "title")
            .append("summary", "summary")
            .append("commentCount", 0L)
            .append("viewCount", 10L)
            .append("publishDate", Instant.parse("2025-01-01T00:00:00Z"))
            .append("createdAt", Instant.now())
            .append("lastViewedAt", Instant.now()),
        "activity_article_views");

    // activity_comments
    mongoTemplate.save(new Document()
            .append("_id", UUID.randomUUID().toString())
            .append("userId", user.getId())
            .append("articleId", article.getId())
            .append("articleTitle", "title")
            .append("content", "활동 댓글")
            .append("likeCount", 0L)
            .append("createdAt", Instant.now())
            .append("userNickname", "act-nick"),
        "activity_comments");

    // activity_comment_likes
    mongoTemplate.save(new Document()
            .append("_id", UUID.randomUUID().toString())
            .append("userId", user.getId()) // likedBy
            .append("commentId", UUID.randomUUID())
            .append("articleId", article.getId())
            .append("articleTitle", "title")
            .append("commentUserId", user.getId())
            .append("commentUserNickname", "act-nick")
            .append("commentContent", "활동 댓글")
            .append("commentLikeCount", 1L)
            .append("commentCreatedAt", Instant.now())
            .append("createdAt", Instant.now()),
        "activity_comment_likes");

    // user_interest_subscriptions
    mongoTemplate.save(new Document()
            .append("_id", UUID.randomUUID().toString())
            .append("user_id", user.getId())
            .append("interest_id", interest.getId())
            .append("interest_name", interest.getName())
            .append("interest_keywords", interest.getKeywords())
            .append("interest_subscriber_count", interest.getSubscriptionsCount())
            .append("created_at", Instant.now()),
        "user_interest_subscriptions");
  }

  @Test
  @Order(1)
  @DisplayName("GET /api/user-activities/me: 200 + 사용자 요약 반환")
  void me_ok() throws Exception {
    mockMvc.perform(get("/api/user-activities/me")
            .with(user(user.getId().toString()).roles("USER"))                 // ★ 인증 주입
            .header("Monew-Request-User-ID", user.getId().toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId().toString()))
        .andExpect(jsonPath("$.email").value("activity@test.com"))
        .andExpect(jsonPath("$.nickname").value("act-nick"));
  }

  @Test
  @Order(2)
  @DisplayName("GET /api/user-activities/me: Mongo 시드 후 각 섹션 크기 ≥ 1")
  void me_sections_filled() throws Exception {
    mockMvc.perform(get("/api/user-activities/me")
            .with(user(user.getId().toString()).roles("USER"))                 // ★ 인증 주입
            .header("Monew-Request-User-ID", user.getId().toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.articleViews.length()").value(greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.comments.length()").value(greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.commentLikes.length()").value(greaterThanOrEqualTo(1)))
        .andExpect(jsonPath("$.subscriptions.length()").value(greaterThanOrEqualTo(1)));
  }

  @Test
  @Order(3)
  @DisplayName("GET /api/user-activities/{userId}: 소유자 접근이면 200")
  void byUserId_owner_ok() throws Exception {
    mockMvc.perform(get("/api/user-activities/{userId}", user.getId())
            .with(user(user.getId().toString()).roles("USER"))                 // ★ 인증 주입
            .header("Monew-Request-User-ID", user.getId().toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId().toString()));
  }

  @Test
  @Order(4)
  @DisplayName("GET /api/user-activities/me: 헤더 없음 → 401")
  void me_unauthorized_ifNoHeader() throws Exception {
    mockMvc.perform(get("/api/user-activities/me")
            .with(user(user.getId().toString()).roles("USER")) // 인증은 있어도 헤더 없음 → 401(컨트롤러 검증)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  // ===== 유틸: User.createdAt 보장 =====
  private static void ensureCreatedAtExists(EntityManager em, UUID userId, Instant ts) {
    int updated = em.createQuery(
            "update User u set u.createdAt = :ts where u.id = :id")
        .setParameter("ts", ts)
        .setParameter("id", userId)
        .executeUpdate();

    if (updated != 1) throw new AssertionError("User.createdAt 업데이트 실패");
    em.clear();

    Instant created = em.createQuery(
            "select u.createdAt from User u where u.id = :id", Instant.class)
        .setParameter("id", userId)
        .getSingleResult();

    if (created == null) throw new AssertionError("User.createdAt 조회 실패");
  }
}
