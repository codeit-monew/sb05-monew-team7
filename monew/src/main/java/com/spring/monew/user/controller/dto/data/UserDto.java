package com.spring.monew.user.controller.dto.data;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDto(
    UUID id,
    String email,
    String nickname,
    LocalDateTime createdAt
) {}
