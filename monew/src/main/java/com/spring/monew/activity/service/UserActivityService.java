package com.spring.monew.activity.service;

import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.comment.domain.Comment;
import com.spring.monew.commentlike.domain.CommentLike;
import com.spring.monew.subscription.domain.Subscription;
import java.util.UUID;

public interface UserActivityService {
  UserActivityDto getUserActivity(UUID userId);

  void addCommentActivity(Comment comment);

  void addCommentLikeActivity(CommentLike commentLike);

  void addSubscriptionActivity(Subscription subscription);

  void removeCommentActivity(UUID commentId);

  void removeCommentLikeActivity(UUID commentLikeId);

  void removeSubscriptionActivity(UUID subscriptionId);
}