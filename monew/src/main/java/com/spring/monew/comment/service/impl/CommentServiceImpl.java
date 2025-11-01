package com.spring.monew.comment.service.impl;

import com.spring.monew.activity.repository.ActivitySyncRepository;
import com.spring.monew.article.domain.Article;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.comment.controller.dto.request.CommentRegisterRequest;
import com.spring.monew.comment.controller.dto.request.CommentUpdateRequest;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.comment.service.CommentService;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ArticleRepository articleRepository;
  private final ActivitySyncRepository activitySyncRepository;

  @Override
  @Transactional
  public CommentDto addComment(CommentRegisterRequest registerRequest) {
    User user = userRepository.findById(registerRequest.userId()).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 유저 입니다."));

    Article article = articleRepository.findById(registerRequest.articleId()).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 기사입니다."));

    Comment comment = commentRepository.save(new Comment(article, user, registerRequest.content()));

    try {
      activitySyncRepository.onCommentCreated(
          comment.getId(),
          user.getId(),
          article.getId(),
          article.getTitle(),
          user.getNickname(),
          comment.getContent(),
          comment.getLikeCount(),
          comment.getCreatedAt()
      );
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (댓글 생성): commentId={}, userId={}, articleId={}",
          comment.getId(), user.getId(), article.getId(), e);
    }

    return new CommentDto(
        comment.getId(),
        comment.getArticle().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount(),
        false,
        comment.getCreatedAt()
    );
  }

  @Override
  @Transactional
  public CursorPageResponseCommentDto getComments(UUID articleId, String orderBy,
      String direction, String cursor, Instant after, int limit, UUID userId) {

    // QueryDSL 다중 조건 검색 필요
    return commentRepository.findCursorPagedComments(articleId, orderBy,
        direction, cursor, after, limit, userId);
  }

  @Override
  @Transactional
  public CommentDto modifyComment(UUID commentId, UUID userId , CommentUpdateRequest updateRequest) {
    Comment comment = commentRepository.findById(commentId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 댓글입니다."));

    if(!userRepository.existsById(userId)) {
      throw new NoSuchElementException("존재하지 않는 회원입니다.");
    }

    comment.update(updateRequest.content());

    Article article = comment.getArticle();
    User writer = comment.getUser();

    try {
      activitySyncRepository.onCommentCreated(
          comment.getId(),
          writer.getId(),
          article.getId(),
          article.getTitle(),
          writer.getNickname(),
          comment.getContent(),
          comment.getLikeCount(),
          comment.getCreatedAt()
      );
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (댓글 수정→스냅샷 갱신): commentId={}, userId={}, articleId={}",
          comment.getId(), writer.getId(), article.getId(), e);
    }

    return new CommentDto(
        comment.getId(),
        comment.getArticle().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount(),
        false,
        comment.getCreatedAt()
    );
  }

  @Override
  @Transactional
  public void removeCommentLogical(UUID commentId) {
    if(!commentRepository.existsById(commentId)) {
      throw new NoSuchElementException("존재하지 않는 댓글입니다.");
    }

    commentRepository.deleteById(commentId);

    try {
      activitySyncRepository.onCommentDeleted(commentId, Instant.now());
    } catch (Exception ignore) {}
  }

  @Override
  @Transactional
  public void removeCommentHard(UUID commentId) {
    if(!commentRepository.existsById(commentId)) {
      throw new NoSuchElementException("존재하지 않는 댓글입니다.");
    }

    commentRepository.deletePhysicalById(commentId);
    try {
      activitySyncRepository.onCommentDeleted(commentId, Instant.now());
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (댓글 물리 삭제): commentId={}", commentId, e);
    }
  }
}
