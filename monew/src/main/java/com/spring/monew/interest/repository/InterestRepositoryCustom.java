package com.spring.monew.interest.repository;

import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InterestRepositoryCustom {

    CursorPageResponseInterestDto findCursorPagedInterests(
            String keyword,
            String orderBy,
            String direction,
            String cursor,
            Instant after,
            int limit,
            UUID userId
    );

    List<String> findSimilarNames(String name, double threshold);
}
