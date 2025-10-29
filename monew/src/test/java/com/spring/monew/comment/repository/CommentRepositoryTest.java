package com.spring.monew.comment.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@ActiveProfiles("test")
@Testcontainers
@DataJpaTest
class CommentRepositoryTest {


  @Container
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

  // postgres 설정
  static {
    postgres.start();
    System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
    System.setProperty("spring.datasource.username", postgres.getUsername());
    System.setProperty("spring.datasource.password", postgres.getPassword());
  }

  @Autowired
  private CommentRepository commentRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ArticleRepository articleRepository;
  @Autowired
  private InterestRepository interestRepository;
  @Autowired
  private JPAQueryFactory queryFactory;

  // ------------------ create entity -----------------
  private Comment saveComment(Article article, User user, String content) {
    Comment comment = new Comment(article, user, content);
    Comment save = commentRepository.save(comment);
    commentRepository.flush();
    return save;
  }

  private @NotNull User createUser() {
    return userRepository.save(new User("user", "email", "password"));
  }

  private @NotNull Article createArticle(String interestName, String sourceURL) {
    Interest interest = interestRepository.save(new Interest(interestName, List.of("A", "B")));

    return articleRepository.save(
        Article.of(interest, ArticleSource.CHOSUN, sourceURL,
            "titleName", Instant.now(), "요약"));
  }

  // ---------------- Test ----------------
  @Test
  @DisplayName("기본 댓글 창 쿼리 테스트")
  void findCursorPagedComments() {
    // given
    User user = createUser();

    Article article = createArticle("테스트1", "http://www.dummy.com");

    Comment c1 = saveComment(article, user, "test1");
    Comment c2 = saveComment(article, user, "test2");
    Comment c3 = saveComment(article, user, "test3");
    // when
    CursorPageResponseCommentDto result = commentRepository.findCursorPagedComments(
        article.getId(), "createdAt", "DESC", null, null, 6, user.getId()
    );

    // then
    assertThat(result.content())
        .extracting(CommentDto::content)
        .containsExactly(c3.getContent(), c2.getContent(), c1.getContent());
  }

  @Test
  @DisplayName("댓글 페이징 - cursor 기반 조회")
  void paging_with_cursor() {

    // given
    User user = createUser();

    Article article = createArticle("테스트1", "http://www.dummy.com");

    Comment c1 = saveComment(article, user, "c1");
    Comment c2 = saveComment(article, user, "c2");
    Comment c3 = saveComment(article, user, "c3");

    CursorPageResponseCommentDto firstPage =
        commentRepository.findCursorPagedComments(
            article.getId(), "createdAt", "DESC", null, null, 2, user.getId());

    String cursor = firstPage.nextCursor();

    CursorPageResponseCommentDto secondPage =
        commentRepository.findCursorPagedComments(
            article.getId(), "createdAt", "DESC", cursor, firstPage.nextAfter(), 2, user.getId());

    assertThat(firstPage.content())
        .extracting(CommentDto::content)
        .containsExactly(c3.getContent(), c2.getContent());
    assertThat(secondPage.content())
        .extracting(CommentDto::content)
        .containsExactly(c1.getContent());
  }

  @Test
  @DisplayName("articleId에 해당하는 댓글만 조회")
  void filterByArticle() {
    User user = createUser();
    Article article1 = createArticle("속도","http://www.dummy1.com");
    Article article2 = createArticle("운동","http://www.dummy2.com");

    saveComment(article1, user, "test article1 - c1");
    saveComment(article2, user, "test article2 - c2");

    CursorPageResponseCommentDto result =
        commentRepository.findCursorPagedComments(
            article1.getId(), "createdAt", "DESC", null, null, 10, user.getId());

    assertThat(result.content()).extracting(CommentDto::content)
        .containsExactly("test article1 - c1");
  }

  @Test
  @DisplayName("soft delete 된 댓글은 조회 결과에서 제외된다")
  void excludeSoftDeleted() {
    // given
    User user = createUser();
    Article article = createArticle("관심사1", "http://www.dummy.com");

    Comment deleted = saveComment(article, user, "삭제대상");

    saveComment(article, user, "정상댓글");

    // when
    commentRepository.delete(deleted);

    CursorPageResponseCommentDto result =
        commentRepository.findCursorPagedComments(article.getId(), "createdAt", "DESC",
            null, null, 10, user.getId());

    // then
    assertThat(result.content()).extracting(CommentDto::content)
        .containsExactly("정상댓글");
  }

  @Test
  @DisplayName("정렬: likeCount DESC 우선")
  void sortByLikeCount() {
    // given
    User user = createUser();
    Article article = createArticle("관심사1", "http://www.dummy.com");

    saveComment(article, user, "like0");
    Comment comment = saveComment(article, user, "like5");
    saveComment(article, user, "like3");

    comment.incrementLikeCount();

    // when
    CursorPageResponseCommentDto result =
        commentRepository.findCursorPagedComments(article.getId(), "likeCount", "DESC",
            null, null, 10, user.getId());


    // then
    assertThat(result.content()).extracting(CommentDto::content)
        .containsExactly("like5", "like3", "like0");
  }



  // 테스트 Configuration 주입
  @TestConfiguration
  static class QuerydslTestConfig {

    @PersistenceContext
    private EntityManager em;

    @Bean
    public JPAQueryFactory queryFactory() {
      return new JPAQueryFactory(em);
    }
  }
}