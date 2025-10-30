package com.spring.monew.commentlike.service.impl;

import com.spring.monew.activity.repository.ActivitySyncService;
import com.spring.monew.article.repository.ArticleRepository;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.commentlike.repository.CommentLikeRepository;
import com.spring.monew.commentlike.service.CommentLikeService;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.subscription.domain.Subscription;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ActivitySyncService activitySyncService;
  private final ArticleRepository articleRepository;

  @Override
  @Transactional
  public CommentLikeDto addCommentLike(UUID commentId, UUID userId) {
    Comment comment = commentRepository.findById(commentId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 댓글 입니다."));

    User user = userRepository.findById(userId).orElseThrow(
        () -> new NoSuchElementException("존재하지 않는 유저 입니다.")
    );

    if(commentLikeRepository.existsByComment_IdAndUser_Id(commentId, userId)){
      throw new IllegalArgumentException("이미 존재하는 좋아요 입니다.");
    }

    comment.incrementLikeCount();
    commentRepository.saveAndFlush(comment);
    CommentLike commentLike = commentLikeRepository.save(new CommentLike(comment, user));

    UUID articleId = comment.getArticle().getId();        // 프록시여도 ID 접근은 안전
    String articleTitle = articleRepository.findTitleOnlyById(articleId);
    if (articleTitle == null) articleTitle = "";          // 널 세이프

    Instant likedAt = (commentLike.getCreatedAt() != null)
        ? commentLike.getCreatedAt() : Instant.now();

    activitySyncService.onCommentLiked(
        commentLike.getId(),
        user.getId(),
        comment.getId(),
        articleId,
        articleTitle,                      // ← 스냅샷으로 저장
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount(),            // 증가된 최신값
        comment.getCreatedAt(),
        likedAt
    );

    return new CommentLikeDto(
        commentLike.getId(),
        commentLike.getUser().getId(),
        commentLike.getCreatedAt(),
        comment.getId(),
        comment.getArticle().getId(),
        comment.getUser().getId(),
        comment.getUser().getNickname(),
        comment.getContent(),
        comment.getLikeCount(),
        comment.getCreatedAt()
    );
  }

  @Override
  @Transactional
  public void removeCommentLike(UUID commentId, UUID userId) {
    CommentLike commentLike = commentLikeRepository.findByComment_IdAndUser_Id(commentId,
            userId)
        .orElseThrow(() -> new NoSuchElementException("존재하지 않는 좋아요 입니다"));

    commentLike.getComment().decrementLikeCount();

    commentLikeRepository.delete(commentLike);
    activitySyncService.onCommentLikeCanceled(commentLike.getId());
  }
}
