package com.spring.monew.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HeaderAuthFilter가 헤더값을 잘 읽고 전달하는지 확인용 컨트롤러
 */
@RestController
class TestAuthFilterChainController {

  @GetMapping("/api/test/header")
  public ResponseEntity<String> checkHeaderAuth(HttpServletRequest request) {

    // Filter가 request에 저장한 userId
    String requestUserId = (String) request.getAttribute("userId");

    // SecurityContext에 저장된 userId
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    String response = String.format("""
                ✅ HeaderAuthFilter 테스트 결과:
                - request.getAttribute("userId") = %s
                - SecurityContextHolder.principal = %s
                """, requestUserId, principal);

    return ResponseEntity.ok(response);
  }
}