package com.spring.monew.activity.service.impl;

import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.repository.UserActivityQueryRepository;
import com.spring.monew.activity.repository.UserActivityQueryRepository.UserSummary;
import com.spring.monew.activity.service.UserActivityService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActivityServiceImpl implements UserActivityService {

  private static final int TOP_N = 10;
  private final UserActivityQueryRepository repo;

  @Override
  @Transactional(readOnly = true)
  public UserActivityDto getUserActivity(UUID userId) {
    UserSummary u = repo.findUserSummary(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    List<UserActivityDto.Subscription> subs = repo.findTopSubscriptions(userId, TOP_N);
    List<UserActivityDto.Comment> comments = repo.findTopComments(userId, TOP_N);
    List<UserActivityDto.CommentLike> likes = repo.findTopCommentLikes(userId, TOP_N);
    List<UserActivityDto.ArticleView> views = repo.findTopArticleViews(userId, TOP_N);

    return new UserActivityDto(
        u.id(), u.email(), u.nickname(), u.createdAt(),
        subs, comments, likes, views
    );
  }
}