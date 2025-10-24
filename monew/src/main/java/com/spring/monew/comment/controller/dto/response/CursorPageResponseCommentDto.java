package com.spring.monew.comment.controller.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * 커서 기반 페이지 응답 (Comment 전용)
 */
public record CursorPageResponseCommentDto(
        List<CommentDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {}
