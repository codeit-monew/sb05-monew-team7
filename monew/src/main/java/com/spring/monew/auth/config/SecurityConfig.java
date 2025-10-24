package com.spring.monew.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 비활성화 (개발용)

        .authorizeHttpRequests(authorize -> authorize
            //  모든 경로 허용 (개발용)
            .anyRequest().permitAll()
            // 회원가입 & 로그인만 접근 허용
            //.requestMatchers(HttpMethod.POST, "/api/users", "/api/users/login").permitAll()
            //  나머지 요청은 인증 필요
            //.anyRequest().authenticated()
        );

    return http.build();
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
