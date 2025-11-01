package com.spring.monew.activity.controller.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * 댓글 활동 DTO
 * - 사용자가 작성한 댓글(또는 조회용 스냅샷)에 대한 요약 정보
 */
public record CommentActivityDto(
    UUID id,
    UUID articleId,
    String articleTitle,
    UUID userId,
    String userNickname,
    String content,
    long likeCount,
    Instant createdAt
) {}
