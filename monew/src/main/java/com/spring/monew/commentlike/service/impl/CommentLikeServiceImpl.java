package com.spring.monew.commentlike.service.impl;

import com.spring.monew.activity.repository.ActivitySyncRepository;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.comment.repository.CommentRepository;
import com.spring.monew.commentlike.controller.dto.response.CommentLikeDto;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.commentlike.repository.CommentLikeRepository;
import com.spring.monew.commentlike.service.CommentLikeService;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.service.NotificationService;
import com.spring.monew.subscription.domain.Subscription;
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
public class CommentLikeServiceImpl implements CommentLikeService {

  private final CommentLikeRepository commentLikeRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;
  private final ActivitySyncRepository activitySyncRepository;
  private final NotificationService notificationService;

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

    CommentLike commentLike = commentLikeRepository.save(new CommentLike(comment, user));

    try {
      activitySyncRepository.onCommentLiked(
          commentLike.getId(),                // likeEventId
          user.getId(),                       // likedByUserId
          comment.getId(),                    // commentId
          comment.getArticle().getId(),       // articleId
          comment.getArticle().getTitle(),    // articleTitleSnapshot
          comment.getUser().getId(),          // commentUserId
          comment.getUser().getNickname(),    // commentUserNicknameSnapshot
          comment.getContent(),               // commentContentSnapshot
          comment.getLikeCount(),             // commentLikeCountSnapshot (long)
          comment.getCreatedAt(),             // commentCreatedAtSnapshot (Instant)
          commentLike.getCreatedAt()          // likedAt (Instant)
      );
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (댓글 좋아요 생성): likeId={}, commentId={}, likedByUserId={}, articleId={}",
          commentLike.getId(), comment.getId(), user.getId(), comment.getArticle().getId(), e);
    }

    UUID commentAuthorId = comment.getUser().getId();
    if (!commentAuthorId.equals(userId)) {
      String content = user.getNickname() + "님이 나의 댓글을 좋아합니다.";
      notificationService.create(
          commentAuthorId,
          content,
          NotificationResourceType.COMMENT, // 관련 리소스 = 댓글
          comment.getId()
      );
    }

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

    try {
      activitySyncRepository.onCommentLikeCanceled(commentLike.getId());
    } catch (Exception e) {
      log.warn("활동 동기화 실패 (댓글 좋아요 취소): likeId={}, commentId={}, userId={}",
          commentLike.getId(), commentId, userId, e);
    }

    commentLikeRepository.delete(commentLike);
  }
}