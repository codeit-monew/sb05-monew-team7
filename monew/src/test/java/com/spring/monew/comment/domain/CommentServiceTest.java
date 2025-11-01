package com.spring.monew.comment.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spring.monew.article.domain.Article;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.request.CommentUpdateRequest;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.comment.service.impl.CommentServiceImpl;
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
class CommentServiceTest {

  @Mock
  private CommentRepository commentRepository;
  @Mock
  private UserRepository userRepository;
  @Mock
  private ArticleRepository articleRepository;

  @InjectMocks
  private CommentServiceImpl commentService;

  UUID userId = UUID.randomUUID();
  UUID articleId = UUID.randomUUID();
  UUID commentId = UUID.randomUUID();

  User user;
  Article article;

  @BeforeEach
  void setup() {
    userId = UUID.randomUUID();
    articleId = UUID.randomUUID();
    commentId = UUID.randomUUID();

    user = new User("nick", "email@e.com", "pass");
    article = Article.of(null, null, "url", "title", Instant.now(), "summary");
  }

  @Test
  @DisplayName("댓글 등록 성공")
  void addComment() {
    CommentRegisterRequest req = new CommentRegisterRequest(articleId, userId, "내용");

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(articleRepository.findById(articleId)).thenReturn(Optional.of(article));
    when(commentRepository.save(any())).thenAnswer(inv -> {
      Comment c = inv.getArgument(0);
      c.setIdForTest(commentId); // 테스트용 ID setter 필요!
      return c;
    });

    CommentDto result = commentService.addComment(req);

    assertThat(result.content()).isEqualTo("내용");
    verify(commentRepository).save(any(Comment.class));
  }

  @Test
  @DisplayName("댓글 등록 실패 - 존재하지 않는 유저")
  void addComment_userNotFound() {
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThatThrownBy(() ->
        commentService.addComment(new CommentRegisterRequest(articleId, userId, "내용")))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("페이징 조회 위임 검증")
  void getComments() {
    CursorPageResponseCommentDto mockResponse =
        new CursorPageResponseCommentDto(List.of(), null, null, 10, 0, false);

    when(commentRepository.findCursorPagedComments(any(), any(), any(), any(), any(), anyInt(),
        any()))
        .thenReturn(mockResponse);

    var result = commentService.getComments(articleId, "createdAt", "DESC", null, null, 10, userId);

    assertThat(result).isNotNull();
    verify(commentRepository).findCursorPagedComments(any(), any(), any(), any(), any(), anyInt(),
        any());
  }

  @Test
  @DisplayName("댓글 수정 성공")
  void modifyComment() {
    Comment comment = new Comment(article, user, "old");
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(userRepository.existsById(userId)).thenReturn(true);

    CommentDto result = commentService.modifyComment(commentId, userId,
        new CommentUpdateRequest("new"));

    assertThat(result.content()).isEqualTo("new");
    verify(commentRepository).findById(commentId);
  }

  @Test
  @DisplayName("댓글 수정 실패 - 유저 없음")
  void modifyComment_userNotFound() {
    Comment comment = new Comment(article, user, "old");
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
    when(userRepository.existsById(userId)).thenReturn(false);

    assertThatThrownBy(() ->
        commentService.modifyComment(commentId, userId, new CommentUpdateRequest("new")))
        .isInstanceOf(NoSuchElementException.class);
  }

  @Test
  @DisplayName("댓글 논리 삭제 성공")
  void removeCommentLogical() {
    Comment comment = new Comment(article, user, "content");
    when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

    commentService.removeCommentLogical(commentId);

    verify(commentRepository).deleteById(commentId);
  }

  @Test
  @DisplayName("댓글 물리 삭제 성공")
  void removeCommentHard() {
    when(commentRepository.existsById(commentId)).thenReturn(true);

    commentService.removeCommentHard(commentId);

    verify(commentRepository).deletePhysicalById(commentId);
  }
}
