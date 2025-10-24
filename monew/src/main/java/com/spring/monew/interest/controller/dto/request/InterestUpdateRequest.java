package com.spring.monew.interest.controller.dto.request;

import java.util.List;

public record InterestUpdateRequest(
        List<String> keywords
) {}
