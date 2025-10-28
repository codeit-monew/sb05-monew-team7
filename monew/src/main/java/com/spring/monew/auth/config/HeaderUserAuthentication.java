package com.spring.monew.auth.config;

import com.spring.monew.user.domain.UserRole;
import java.util.Collections;
import java.util.List;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

//스프링 시큐리티의 인증 토큰 기반 클래스 상속
//JWT,헤더 등 다양한 인증방식이 이걸 상속해서 구현
public class HeaderUserAuthentication extends AbstractAuthenticationToken {

  /**
   * 요청 헤더에서 전달된 사용자 ID(UUID)와 권한 정보를 담는 클래스
   */

  private final String userId; //헤더에서 추출한 사용자 ID(UUID) 담는 필드
  private final UserRole role;  // 사용자 권한 (USER / ADMIN)

  // 권한이 없는 기본 생성자
  public HeaderUserAuthentication(String userId) {
    super(Collections.emptyList()); // 권한 없음
    this.userId = userId;
    this.role = null;
    setAuthenticated(true);
  }

  // 권한이 있는 생성자
  public HeaderUserAuthentication(String userId, UserRole role) {
    super(List.of(new SimpleGrantedAuthority("ROLE_" + role.name())));
    this.userId = userId;
    this.role = role;
    setAuthenticated(true);
  }

  @Override
  public Object getPrincipal() {
    return userId;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  public UserRole getRole() {
    return role;
  }
}

