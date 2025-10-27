package com.spring.monew.user.controller;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}
