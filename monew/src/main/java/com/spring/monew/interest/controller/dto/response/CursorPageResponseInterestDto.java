package com.spring.monew.interest.controller.dto.response;

import java.time.Instant;
import java.util.List;

public record CursorPageResponseInterestDto(
        List<InterestDto> content,
        String nextCursor,
        Instant nextAfter,
        int size,
        long totalElements,
        boolean hasNext
) {}
