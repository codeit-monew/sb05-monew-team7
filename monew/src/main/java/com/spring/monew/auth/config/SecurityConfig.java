package com.spring.monew.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.spring.monew.common.filter.RequestIdFilter;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final HeaderAuthFilter headerAuthFilter;
  private final RequestIdFilter requestIdFilter;

  @Value("${monitoring.prometheus.allow-ip:127.0.0.1}")
  private String prometheusAllowIp;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/actuator/prometheus")
            .access((authentication, context) -> {
              String remoteAddr = context.getRequest().getRemoteAddr();
              boolean allowed =
                  remoteAddr.equals("127.0.0.1") ||
                      remoteAddr.equals("0:0:0:0:0:0:0:1") || // IPv6 localhost
                      remoteAddr.equals("localhost") ||
                      remoteAddr.startsWith("172.") || // Docker 내부 네트워크
                      remoteAddr.startsWith("192.168.") ||
                      remoteAddr.equals(prometheusAllowIp);

              return new AuthorizationDecision(allowed);
            })
            .requestMatchers("/actuator/health", "/actuator/info",
                "/actuator/loggers").permitAll() //Actuator 허용 (원래는 이렇게 하면 안됨)
                //  모든 경로 허용 (개발용)
            .requestMatchers("/api/batch/**").permitAll()
            .anyRequest().permitAll()
        )
            // 헤더에 담긴 userId를 읽어서 인증 정보를 만들어주는 필터
        .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
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
