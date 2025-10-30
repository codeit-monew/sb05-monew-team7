package com.spring.monew.user.controller;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.controller.dto.request.UserUpdateRequest;
import com.spring.monew.user.service.UserService;
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
public class UserController {

  private final UserService userService;

  /**
   * 회원가입 요청 처리
   */
  @PostMapping
  public ResponseEntity<UserDto> userAdd(@RequestBody @Valid UserRegisterRequest request) {
    UserDto userDto = userService.addUser(request);
    return ResponseEntity.status(201).body(userDto);
  }

  /**
   * 사용자 정보 수정(닉네임 변경)
   */

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
  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> userRemoveLogical(@PathVariable UUID userId) {
    userService.removeUserLogical(userId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 사용자 정보 물리 삭제
   */
  @DeleteMapping("/{userId}/hard")
  public ResponseEntity<Void> userRemoveHard(@PathVariable UUID userId) {
    userService.removeUserHard(userId);
    return ResponseEntity.noContent().build();
  }

}


