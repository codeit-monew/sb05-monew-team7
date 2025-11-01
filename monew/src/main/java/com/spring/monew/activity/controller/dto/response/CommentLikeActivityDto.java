package com.spring.monew.activity.controller.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * 댓글 좋아요 활동 DTO
 * - 사용자가 좋아요를 누른 댓글에 대한 스냅샷 정보
 */
public record CommentLikeActivityDto(
    UUID id,
    Instant createdAt,
    UUID commentId,
    UUID articleId,
    String articleTitle,
    UUID commentUserId,
    String commentUserNickname,
    String commentContent,
    long commentLikeCount,
    Instant commentCreatedAt
) {}
