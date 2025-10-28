package com.spring.monew.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

  /* 프론트에서 보낸 헤더(MoNew-Request-User-ID) 값을 읽고
   * SecurityContext와 Request에 저장하는 클래스
   * */

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {

    // 헤더에서 사용자 ID 읽기
    String userId = request.getHeader("MoNew-Request-User-ID");
    System.out.println("🟢 [HeaderAuthFilter] 들어온 헤더값 = " + userId);


    // 헤더가 존재할 때만 인증 정보 저장
    if (userId != null && !userId.isBlank()) {
      HeaderUserAuthentication authentication = new HeaderUserAuthentication(userId);

      // SecurityContextHolder에 인증 정보 저장
      SecurityContextHolder.getContext().setAuthentication(authentication);

      // Controller에서 편하게 꺼낼 수 있도록 request에도 저장
      request.setAttribute("userId", userId);
    } else {
      System.out.println("⚠️ [HeaderAuthFilter] userId 헤더 없음");
    }

    // 다음 필터로 요청 전달
    filterChain.doFilter(request, response);
  }
}
