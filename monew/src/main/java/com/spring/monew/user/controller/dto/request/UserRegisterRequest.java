package com.spring.monew.user.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRegisterRequest(

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    String email,

    @NotBlank(message = "닉네임은 필수입니다.")
    @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하로 입력해주세요.")
    String nickname,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=`~{}\\[\\]:\";'<>?,./]).{6,}$",
        message = "영문과 숫자, 특수문자를 포함해 6자 이상 입력해 주세요."
    )
    String password

) {}