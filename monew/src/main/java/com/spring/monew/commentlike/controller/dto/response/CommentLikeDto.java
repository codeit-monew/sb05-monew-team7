package com.spring.monew.commentlike.controller.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentLikeDto(
        UUID id,                      // 좋아요 ID
        UUID likedBy,                 // 좋아요한 사용자 ID
        Instant createdAt,            // 좋아요한 날짜
        UUID commentId,               // 댓글 ID
        UUID articleId,               // 기사 ID
        UUID commentUserId,           // 작성자 ID
        String commentUserNickname,   // 작성자 닉네임
        String commentContent,        // 내용
        long commentLikeCount,        // 좋아요 수
        Instant commentCreatedAt      // 댓글 작성 시각
) {}
