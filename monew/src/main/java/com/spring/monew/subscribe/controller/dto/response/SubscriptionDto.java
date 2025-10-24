package com.spring.monew.subscribe.controller.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SubscriptionDto(
        UUID id,
        UUID interestId,
        String interestName,
        List<String> interestKeywords,
        long interestSubscriberCount,
        Instant createdAt
) {}
