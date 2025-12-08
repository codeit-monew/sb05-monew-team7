package com.spring.monew.interest.service;

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
import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
@Transactional
class InterestIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @Autowired private InterestRepository interestRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private EntityManager entityManager;

  private User user;

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("monew_test")
          .withUsername("postgres")
          .withPassword("1234")
          .withInitScript("schema.sql");

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  @BeforeEach
  void setup() {
    user = userRepository.save(new User("email@test.com", "nick", "password"));
  }


  @Test
  @DisplayName("관심사 등록 성공")
  void addInterest() throws Exception {
    InterestRegisterRequest request =
        new InterestRegisterRequest("테스트", List.of("야구", "축구", "농구"));

    mockMvc.perform(post("/api/interests")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("테스트"))
        .andExpect(jsonPath("$.keywords[0]").value("야구"));
  }

  @Test
  @DisplayName("관심사 목록 조회 - 기본 ASC 정렬")
  void getInterests() throws Exception {
    interestRepository.save(new Interest("A", List.of("dev")));
    interestRepository.save(new Interest("B", List.of("code")));
    interestRepository.save(new Interest("C", List.of("infra")));

    mockMvc.perform(get("/api/interests")
            .param("orderBy", "name")
            .param("direction", "ASC")
            .param("limit", "2")
            .header("Monew-Request-User-ID", user.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content[0].name").value("A"));
  }

  @Test
  @DisplayName("관심사 수정 성공")
  void modifyInterest() throws Exception {
    Interest saved = interestRepository.save(new Interest("테스트", List.of("old", "tags")));

    InterestUpdateRequest updateRequest =
        new InterestUpdateRequest(List.of("new", "updated"));

    mockMvc.perform(patch("/api/interests/{interestId}", saved.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keywords[0]").value("new"))
        .andExpect(jsonPath("$.keywords[1]").value("updated"));
  }

  @Test
  @DisplayName("관심사 삭제 성공 - 관련 게시글도 소프트 삭제됨")
  void removeInterest() throws Exception {
    Interest saved = interestRepository.save(new Interest("삭제대상", List.of("tag")));
    UUID interestId = saved.getId();
    
    Article article1 = Article.of(saved, ArticleSource.NAVER, 
        "https://example.com/1", "제목1", Instant.now(), "요약1");
    Article article2 = Article.of(saved, ArticleSource.NAVER, 
        "https://example.com/2", "제목2", Instant.now(), "요약2");
    
    articleRepository.save(article1);
    articleRepository.save(article2);
    
    UUID article1Id = article1.getId();
    UUID article2Id = article2.getId();
    
    entityManager.flush();
    entityManager.clear();

    mockMvc.perform(delete("/api/interests/{interestId}", interestId))
        .andExpect(status().isOk());

    entityManager.flush();
    entityManager.clear();
    
    Boolean interestIsDeleted = (Boolean) entityManager.createNativeQuery(
        "SELECT is_deleted FROM interests WHERE id = CAST(?1 AS uuid)")
        .setParameter(1, interestId.toString())
        .getSingleResult();
    assertThat(interestIsDeleted).isTrue();
    
    Boolean article1IsDeleted = (Boolean) entityManager.createNativeQuery(
        "SELECT is_deleted FROM articles WHERE id = CAST(?1 AS uuid)")
        .setParameter(1, article1Id.toString())
        .getSingleResult();
    assertThat(article1IsDeleted).isTrue();
    
    Boolean article2IsDeleted = (Boolean) entityManager.createNativeQuery(
        "SELECT is_deleted FROM articles WHERE id = CAST(?1 AS uuid)")
        .setParameter(1, article2Id.toString())
        .getSingleResult();
    assertThat(article2IsDeleted).isTrue();
    
    assertThat(interestRepository.findById(interestId).isEmpty()).isTrue();
    assertThat(articleRepository.findById(article1Id).isEmpty()).isTrue();
    assertThat(articleRepository.findById(article2Id).isEmpty()).isTrue();
  }

  @Test
  @DisplayName("존재하지 않는 관심사 수정 시 예외 발생")
  void modifyInterest_NotFound() throws Exception {
    InterestUpdateRequest updateRequest =
        new InterestUpdateRequest(List.of("hi"));

    mockMvc.perform(patch("/api/interests/{interestId}", UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(status().is4xxClientError());
  }
}
