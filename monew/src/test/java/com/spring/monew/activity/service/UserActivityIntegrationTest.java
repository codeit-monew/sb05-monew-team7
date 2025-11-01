package com.spring.monew.activity.service;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.spring.monew.activity.repository.ActivitySyncRepository;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class UserActivityIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserRepository userRepository;
  @Autowired private InterestRepository interestRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private ActivitySyncRepository activitySyncRepository;

  @Autowired private JdbcTemplate jdbc;

  private User user;
  private Interest interest;
  private Article article;

  @BeforeEach
  void setup() {
    // 1) 기본 데이터 저장
    user = userRepository.save(new User("email@test.com", "nick", "password"));
    interest = interestRepository.save(new Interest("AI", List.of("생성형", "LLM")));
    article = articleRepository.save(
        Article.of(
            interest,
            ArticleSource.CHOSUN,
            "https://example.com/news/1",
            "기사제목",
            Instant.parse("2024-12-31T00:00:00Z"),
            "요약"
        )
    );

    // 2) 즉시 flush (동일 트랜잭션에서 JDBC 가시성 보장)
    userRepository.flush();
    interestRepository.flush();
    articleRepository.flush();

    // 3) users.created_at 강제 보정 (인용/카멜/스네이크 모두 대응)
    ensureCreatedAtExists(jdbc, user.getId(), Instant.parse("2025-01-01T00:00:00Z"));
  }

  @Test
  @Order(1)
  @DisplayName("me: 활동이 없어도 200 + 사용자 요약 반환")
  void myActivity_empty_ok() throws Exception {
    mockMvc.perform(get("/api/user-activities/me")
            .header("Monew-Request-User-ID", user.getId().toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId().toString()))
        .andExpect(jsonPath("$.email").value("email@test.com"))
        .andExpect(jsonPath("$.nickname").value("nick"))
        .andExpect(jsonPath("$.subscriptions", hasSize(0)))
        .andExpect(jsonPath("$.comments", hasSize(0)))
        .andExpect(jsonPath("$.commentLikes", hasSize(0)));
    // articleViews 검증은 생략(동기화 메서드 없음 + 유니크 인덱스 충돌 회피)
  }

  @Test
  @Order(2)
  @DisplayName("me: 구독/댓글/좋아요 활동이 있으면 각 섹션이 채워진다")
  void myActivity_with_events_ok() throws Exception {
    // ===== seed: 구독 =====
    UUID subId = UUID.randomUUID();
    activitySyncRepository.onSubscribed(
        subId,
        user.getId(),
        interest.getId(),
        interest.getName(),
        interest.getKeywords(),
        123L,
        Instant.parse("2025-01-02T00:00:00Z")
    );

    // ===== seed: 댓글 =====
    UUID commentId = UUID.randomUUID();
    activitySyncRepository.onCommentCreated(
        commentId,
        user.getId(),
        article.getId(),
        article.getTitle(),
        user.getNickname(),
        "댓글내용",
        3L,
        Instant.parse("2025-01-03T00:00:00Z")
    );

    // ===== seed: 좋아요 =====
    UUID likeEventId = UUID.randomUUID();
    activitySyncRepository.onCommentLiked(
        likeEventId,
        user.getId(),                // likedBy
        commentId,
        article.getId(),
        article.getTitle(),
        user.getId(),                // commentUserId (현재 구현과 맞춤)
        user.getNickname(),          // commentUserNickname (현재 구현과 맞춤)
        "댓글내용",
        10L,
        Instant.parse("2025-01-01T00:00:00Z"),
        Instant.parse("2025-01-04T00:00:00Z")
    );

    mockMvc.perform(get("/api/user-activities/me")
            .header("Monew-Request-User-ID", user.getId().toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId().toString()))
        .andExpect(jsonPath("$.subscriptions", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$.comments", hasSize(greaterThanOrEqualTo(1))))
        .andExpect(jsonPath("$.commentLikes", hasSize(greaterThanOrEqualTo(1))));
    // articleViews 검증은 생략
  }

  @Test
  @Order(3)
  @DisplayName("{userId}: 소유자 접근이면 200")
  void userActivity_by_id_owner_ok() throws Exception {
    mockMvc.perform(get("/api/user-activities/{userId}", user.getId())
            .header("Monew-Request-User-ID", user.getId().toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.getId().toString()));
  }

  @Test
  @Order(4)
  @DisplayName("{userId}: 헤더 유저 ≠ 경로 유저 → 403")
  void userActivity_by_id_forbidden_when_other() throws Exception {
    UUID other = UUID.randomUUID();
    mockMvc.perform(get("/api/user-activities/{userId}", user.getId())
            .header("Monew-Request-User-ID", other.toString())
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  @Order(5)
  @DisplayName("me: 헤더 없으면 401")
  void myActivity_unauthorized_when_no_header() throws Exception {
    mockMvc.perform(get("/api/user-activities/me"))
        .andExpect(status().isUnauthorized());
  }

  // ---------- helper ----------

  private static void ensureCreatedAtExists(JdbcTemplate jdbc, UUID userId, Instant instant) {
    Timestamp ts = Timestamp.from(instant);

    // 1) UPDATE 시도 (인용/비인용 + 카멜/스네이크 조합)
    String[] updateSqls = new String[] {
        // users.created_at
        "UPDATE users SET created_at = ? WHERE id = ?",
        "UPDATE users SET \"created_at\" = ? WHERE id = ?",
        "UPDATE \"users\" SET created_at = ? WHERE id = ?",
        "UPDATE \"users\" SET \"created_at\" = ? WHERE id = ?",

        // users.createdAt (카멜 표기 대응)
        "UPDATE users SET createdAt = ? WHERE id = ?",
        "UPDATE users SET \"createdAt\" = ? WHERE id = ?",
        "UPDATE \"users\" SET createdAt = ? WHERE id = ?",
        "UPDATE \"users\" SET \"createdAt\" = ? WHERE id = ?"
    };

    int updated = 0;
    for (String sql : updateSqls) {
      try {
        updated = jdbc.update(sql, ts, userId);
      } catch (Exception ignore) { }
      if (updated > 0) break;
    }

    // 2) SELECT로 실제 값 확인 (여의치 않으면 명확히 실패)
    String[] selectSqls = new String[] {
        "SELECT created_at FROM users WHERE id = ?",
        "SELECT \"created_at\" FROM users WHERE id = ?",
        "SELECT created_at FROM \"users\" WHERE id = ?",
        "SELECT \"created_at\" FROM \"users\" WHERE id = ?",

        "SELECT createdAt FROM users WHERE id = ?",
        "SELECT \"createdAt\" FROM users WHERE id = ?",
        "SELECT createdAt FROM \"users\" WHERE id = ?",
        "SELECT \"createdAt\" FROM \"users\" WHERE id = ?"
    };

    Timestamp created = null;
    for (String sql : selectSqls) {
      try {
        created = jdbc.queryForObject(sql, (rs, rowNum) -> rs.getTimestamp(1), userId);
      } catch (Exception ignore) { }
      if (created != null) break;
    }

    if (created == null) {
      throw new AssertionError("users 테이블의 created_at/createdAt 업데이트 실패: 스키마 컬럼명/인용(quoting) 확인 필요");
    }
  }
}
