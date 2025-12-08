package com.spring.monew.commentlike.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.common.config.QuerydslConfig;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(QuerydslConfig.class)
@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class CommentLikeRepositoryTest {

  @Autowired private CommentLikeRepository commentLikeRepository;
  @Autowired private CommentRepository commentRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private ArticleRepository articleRepository;
  @Autowired private InterestRepository interestRepository;

  private CommentLike prepareLike() {
    User user = userRepository.save(new User("email@test.com", "nick", "password"));
    Interest interest = interestRepository.save(new Interest("it", List.of("java", "spring")));
    Article article =
        articleRepository.save(
            Article.of(interest, ArticleSource.CHOSUN, "http://dummy.com", "title", Instant.now(), "summary"));
    Comment comment = commentRepository.save(new Comment(article, user, "좋아요 테스트 댓글"));

    return commentLikeRepository.save(new CommentLike(comment, user));
  }

  @Test
  @DisplayName("existsByComment_IdAndUser_Id - 존재하면 true 반환")
  void existsByCommentAndUserId_true() {
    CommentLike like = prepareLike();

    boolean exists =
        commentLikeRepository.existsByComment_IdAndUser_Id(
            like.getComment().getId(), like.getUser().getId());

    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsByComment_IdAndUser_Id - 존재하지 않으면 false 반환")
  void existsByCommentAndUserId_false() {
    CommentLike like = prepareLike();

    boolean exists =
        commentLikeRepository.existsByComment_IdAndUser_Id(
            UUID.randomUUID(), like.getUser().getId());

    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("findByComment_IdAndUser_Id - 존재 시 Optional 반환")
  void findByCommentAndUserId_present() {
    CommentLike like = prepareLike();

    Optional<CommentLike> found =
        commentLikeRepository.findByComment_IdAndUser_Id(
            like.getComment().getId(), like.getUser().getId());

    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(like.getId());
  }

  @Test
  @DisplayName("findByComment_IdAndUser_Id - 존재하지 않으면 Optional.empty() 반환")
  void findByCommentAndUserId_empty() {
    prepareLike();

    Optional<CommentLike> found =
        commentLikeRepository.findByComment_IdAndUser_Id(UUID.randomUUID(), UUID.randomUUID());

    assertThat(found).isEmpty();
  }
}
