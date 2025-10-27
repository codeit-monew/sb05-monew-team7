package com.spring.monew.auth.controller;


import com.spring.monew.auth.service.AuthService;
import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserLoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/api/users/login")
  public ResponseEntity<UserDto> login(@RequestBody @Valid UserLoginRequest request) {
    UserDto userDto = authService.login(request);
    return ResponseEntity.ok(userDto);
  }
}

