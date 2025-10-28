package com.spring.monew.user.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 20, message = "닉네임은 1~20자 사이여아 합니다." )
    String nickname
){}