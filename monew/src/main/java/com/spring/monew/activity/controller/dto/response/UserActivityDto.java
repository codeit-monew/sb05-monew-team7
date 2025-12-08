package com.spring.monew.activity.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.spring.monew.articleview.controller.dto.response.ArticleViewDto;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserActivityDto(
    UUID id,
    String email,
    String nickname,
    @JsonFormat(shape = JsonFormat.Shape.STRING, timezone = "UTC") Instant createdAt,
    List<SubscriptionDto> subscriptions,
    List<CommentActivityDto> comments,
    List<CommentLikeActivityDto> commentLikes,
    List<ArticleViewDto> articleViews
) {
}