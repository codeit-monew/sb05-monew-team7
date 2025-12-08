package com.spring.monew.interest.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.monew.common.config.QuerydslConfig;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.impl.InterestRepositoryCustomImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@DataJpaTest
@Import(QuerydslConfig.class)
// findSimilarity 테스트가 Pg_trgm을 기본적으로 필요로 해서 이거만 postgres 사용함.
class InterestRepositoryTest {

  @Container
  static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:15-alpine")
          .withDatabaseName("monew_test")
          .withUsername("postgres")
          .withPassword("1234")
          .withInitScript("schema.sql"); // 자동으로 CREATE EXTENSION 실행

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  @Autowired
  private InterestRepository interestRepository;

  @Autowired
  private JPAQueryFactory queryFactory;

  private InterestRepositoryCustomImpl interestRepositoryCustom;

  @BeforeEach
  void setUp() {
    interestRepositoryCustom = new InterestRepositoryCustomImpl(queryFactory);
  }

  @Test
  @DisplayName("기본 커서 기반 조회 테스트")
  void findCursorPagedInterests_basic() {
    // given
    interestRepository.save(new Interest("야구", List.of("스포츠")));
    interestRepository.save(new Interest("축구", List.of("스포츠")));
    interestRepository.save(new Interest("농구", List.of("운동")));

    // when
    var result = interestRepositoryCustom.findCursorPagedInterests(
        null, "name", "ASC", null, null, 2, UUID.randomUUID()
    );

    // then
    assertThat(result.content()).hasSize(2);
    assertThat(result.hasNext()).isTrue();
  }

  @Test
  @DisplayName("유사 이름 검색 - 낮은 유사도")
  void findSimilarNames_lowThreshold() {
    interestRepository.save(new Interest("야구", List.of("스포츠")));
    interestRepository.save(new Interest("축구", List.of("운동")));
    interestRepository.save(new Interest("야구뉴스", List.of("기사")));
    interestRepository.save(new Interest("야구선수", List.of("선수")));
    interestRepository.save(new Interest("게임", List.of("취미")));

    List<String> similarNames = interestRepositoryCustom.findSimilarNames("야구", 0.2);

    assertThat(similarNames)
        .contains("야구뉴스", "야구선수")
        .doesNotContain("게임", "축구");
  }

  @Test
  @DisplayName("유사 이름 검색 - 높은 유사도")
  void findSimilarNames_highThreshold() {
    interestRepository.save(new Interest("스포츠 센터", List.of("스포츠")));
    interestRepository.save(new Interest("스포츠 센타", List.of("선수")));
    interestRepository.save(new Interest("축구", List.of("운동")));

    List<String> similarNames = interestRepositoryCustom.findSimilarNames("스포츠 센터", 0.5);

    assertThat(similarNames)
        .contains("스포츠 센타")
        .doesNotContain("축구");
  }
}