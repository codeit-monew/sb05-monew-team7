package com.spring.monew.auth.controller;


import com.spring.monew.auth.service.AuthService;
import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserLoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "사용자 로그인 관리", description = "사용자 로그인 관련 API")
public class AuthController {

  private final AuthService authService;

  @Operation(
      summary = "로그인",
      description = "사용자 로그인을 처리합니다."
  )
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "로그인 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "401", description = "로그인 실패 (이메일 또는 비밀번호 불일치)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/api/users/login")
  public ResponseEntity<UserDto> login(@RequestBody @Valid UserLoginRequest request) {
    UserDto userDto = authService.login(request);
    return ResponseEntity.ok(userDto);
  }
}