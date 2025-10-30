package com.spring.monew.commentlike.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.repository.CommentLikeRepository;
import com.spring.monew.commentlike.service.impl.CommentLikeServiceImpl;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
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

  private UUID userId;
  private UUID commentId;
  private User user;
  private Comment comment;

  @BeforeEach
  void setup() {
    userId = UUID.randomUUID();
    commentId = UUID.randomUUID();
    user = new User("email@test.com", "nick", "password");
    Article article = Article.of(mock(Interest.class), ArticleSource.CHOSUN, "http://dummy.com",
        "title", Instant.now(), "요약");
    comment = new Comment(article, user, "테스트 댓글");
  }

  @Test
  @DisplayName("좋아요 추가 성공")
  void addCommentLike_success() {
    // given
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(commentLikeRepository.existsByComment_IdAndUser_Id(commentId, userId)).willReturn(false);
    given(commentLikeRepository.save(any(CommentLike.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // when
    CommentLikeDto result = commentLikeService.addCommentLike(commentId, userId);

    // then
    assertThat(result).isNotNull();
    then(commentRepository).should().findById(commentId);
    then(commentLikeRepository).should().save(any(CommentLike.class));
  }

  @Test
  @DisplayName("이미 좋아요가 존재하면 예외 발생")
  void addCommentLike_duplicateError() {
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(commentLikeRepository.existsByComment_IdAndUser_Id(commentId, userId)).willReturn(true);

    assertThatThrownBy(() -> commentLikeService.addCommentLike(commentId, userId))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("이미 존재하는 좋아요");

    then(commentLikeRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("존재하지 않는 댓글이면 예외 발생")
  void addCommentLike_noComment() {
    given(commentRepository.findById(commentId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> commentLikeService.addCommentLike(commentId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지 않는 댓글");
  }

  @Test
  @DisplayName("존재하지 않는 유저면 예외 발생")
  void addCommentLike_noUser() {
    given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> commentLikeService.addCommentLike(commentId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지 않는 유저");
  }

  @Test
  @DisplayName("좋아요 삭제 성공")
  void removeCommentLike_success() {
    CommentLike like = new CommentLike(comment, user);
    given(commentLikeRepository.findByComment_IdAndUser_Id(commentId, userId))
        .willReturn(Optional.of(like));

    commentLikeService.removeCommentLike(commentId, userId);

    then(commentLikeRepository).should().delete(like);
  }

  @Test
  @DisplayName("존재하지 않는 좋아요 삭제 시 예외 발생")
  void removeCommentLike_notFound() {
    given(commentLikeRepository.findByComment_IdAndUser_Id(commentId, userId))
        .willReturn(Optional.empty());

    assertThatThrownBy(() -> commentLikeService.removeCommentLike(commentId, userId))
        .isInstanceOf(NoSuchElementException.class)
        .hasMessageContaining("존재하지 않는 좋아요");
  }
}