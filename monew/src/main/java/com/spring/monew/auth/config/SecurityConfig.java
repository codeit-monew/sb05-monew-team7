package com.spring.monew.auth.config;

import lombok.RequiredArgsConstructor;
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

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final HeaderAuthFilter headerAuthFilter;
  private final RequestIdFilter requestIdFilter;

  @Value("${monitoring.prometheus.allow-ip}")
  private String prometheusAllowIp;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)

        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/actuator/prometheus")
            .access((authentication, context) -> {
              String remoteAddr = context.getRequest().getRemoteAddr();
              boolean equals = remoteAddr.equals(prometheusAllowIp);
              return new AuthorizationDecision(equals);
            })
            .requestMatchers("/actuator/health", "/actuator/info",
                "/actuator/loggers").permitAll()
            .requestMatchers("/api/batch/**").permitAll()
            .anyRequest().permitAll()
        )
        .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(headerAuthFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }


  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
