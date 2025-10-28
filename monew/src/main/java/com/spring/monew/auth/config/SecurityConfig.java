package com.spring.monew.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final HeaderAuthFilter headerAuthFilter;


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) // CSRF 보안 비활성화 (개발용)

        .authorizeHttpRequests(authorize -> authorize
            //  모든 경로 허용 (개발용)
            .anyRequest().permitAll()
            )
            // 헤더에 담긴 userId를 읽어서 인증 정보를 만들어주는 필터
        .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);
            //.requestMatchers(HttpMethod.POST, "/api/users", "/api/users/login").permitAll()
            //  나머지 요청은 인증 필요
            //.anyRequest().authenticated()

    return http.build();
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
