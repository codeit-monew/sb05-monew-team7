package com.spring.monew.commentlike.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.commentlike.repository.CommentLikeRepository;
import com.spring.monew.commentlike.service.impl.CommentLikeServiceImpl;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentLikeServiceTest {

  @Mock private CommentLikeRepository commentLikeRepository;
  @Mock private CommentRepository commentRepository;
  @Mock private UserRepository userRepository;

  @InjectMocks private CommentLikeServiceImpl commentLikeService;

  private UUID commentLikeId;
  private UUID commentId;
  private UUID userId;

  private User user;
  private Article article;
  private Comment comment;

  @BeforeEach
  void setUp() {
    commentLikeId = UUID.randomUUID();
    commentId = UUID.randomUUID();
    userId = UUID.randomUUID();

    user = new User("email@gmail.com", "user", "password");

    article =
        new Article(
            UUID.randomUUID(),
            new com.spring.monew.interest.domain.Interest("관심사 테스트", List.of()),
            ArticleSource.CHOSUN,
            "https://test.com/news/123",
            "테스트 기사",
            Instant.now(),
            "이것은 테스트용 기사 요약",
            3L, // commentCount
            50L, // viewCount
            false, // isDeleted
            Instant.now(),
            Instant.now());

    comment = new Comment(commentId, user, article, "내용", false, 0, Instant.now());
  }

  @Test
  @DisplayName("댓글 좋아요 추가 성공")
  void addCommentLike_success() {
    // given
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(commentLikeRepository.existsByComment_IdAndUser_Id(commentId, userId))
        .willReturn(false);

    CommentLike savedLike =
        new CommentLike(commentLikeId, comment, user, Instant.now());

    given(commentLikeRepository.save(any(CommentLike.class))).willReturn(savedLike);

    // when
    CommentLike result = commentLikeService.addCommentLike(commentId, userId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getUser()).isEqualTo(user);
    assertThat(result.getComment()).isEqualTo(comment);
    verify(commentLikeRepository).save(any(CommentLike.class));
  }

  @Test
  @DisplayName("댓글 좋아요 추가 실패 - 존재하지 않는 댓글")
  void addCommentLike_commentNotFound() {
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> commentLikeService.addCommentLike(commentId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지 않는 댓글");
  }

  @Test
  @DisplayName("댓글 좋아요 추가 실패 - 이미 존재하는 좋아요")
  void addCommentLike_alreadyExists() {
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(commentLikeRepository.existsByComment_IdAndUser_Id(commentId, userId)).willReturn(true);

    assertThatThrownBy(() -> commentLikeService.addCommentLike(commentId, userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("이미 존재하는 좋아요");
  }

  @Test
  @DisplayName("댓글 좋아요 제거 성공")
  void removeCommentLike_success() {
    // given
    CommentLike commentLike =
        new CommentLike(commentLikeId, comment, user, Instant.now());

    given(commentLikeRepository.findByComment_IdAndUser_Id(commentId, userId))
        .willReturn(Optional.of(commentLike));

    // when
    CommentLike result = commentLikeService.removeCommentLike(commentId, userId);

    // then
    assertThat(result).isNotNull();
    assertThat(result.getComment()).isEqualTo(comment);
    verify(commentLikeRepository).delete(commentLike);
  }

  @Test
  @DisplayName("댓글 좋아요 제거 실패 - 존재하지 않음")
  void removeCommentLike_notFound() {
    given(commentLikeRepository.findByComment_IdAndUser_Id(commentId, userId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> commentLikeService.removeCommentLike(commentId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지 않는 좋아요");
  }
}
