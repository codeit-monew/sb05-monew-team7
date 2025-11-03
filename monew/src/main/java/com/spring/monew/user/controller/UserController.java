package com.spring.monew.user.controller;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.controller.dto.request.UserUpdateRequest;
import com.spring.monew.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 관리", description = "사용자 관련 API")
public class UserController {

  private final UserService userService;

  /**
   * 회원가입 요청 처리
   */
  @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "회원가입 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "49", description = "이메일 중복"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping
  public ResponseEntity<UserDto> userAdd(@RequestBody @Valid UserRegisterRequest request) {
    UserDto userDto = userService.addUser(request);
    return ResponseEntity.status(201).body(userDto);
  }

  /**
   * 사용자 정보 수정(닉네임 변경)
   */
  @Operation(summary = "사용자 정보 수정", description = "사용자의 닉네임을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "사용자 정보 수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "403", description = "사용자 정보 수정 권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{userId}")
  public ResponseEntity<UserDto> userModify(
      @PathVariable UUID userId,
      @RequestBody @Valid UserUpdateRequest updateRequest
  ) {
    UserDto updated = userService.modifyUser(userId, updateRequest);
    return ResponseEntity.ok(updated);
  }

  /**
   * 사용자 정보 논리 삭제
   */
  @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
      @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> userRemoveLogical(@PathVariable UUID userId) {
    userService.removeUserLogical(userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 사용자 정보 물리 삭제
   */
  @Operation(summary = "사용자 물리 삭제", description = "사용자를 물리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "사용자 삭제 성공"),
      @ApiResponse(responseCode = "403", description = "사용자 삭제 권한 없음"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> userRemoveHard(@PathVariable UUID userId) {
    userService.removeUserHard(userId);
    return ResponseEntity.noContent().build();
  }

}


