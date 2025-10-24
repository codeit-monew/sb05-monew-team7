package com.spring.monew.user.service;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.domain.UserRole;
import com.spring.monew.user.repository.UserRepository;
import com.sun.nio.sctp.IllegalReceiveException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  /**
   * 회원 등록 처리
   */
  public UserDto addUser(UserRegisterRequest request) {
    if (userRepository.existsByEmail(request.email())) {
      throw new IllegalReceiveException("이미 사용 중인 이메일입니다.");
    }

    User user = User.builder()
        .email(request.email())
        .nickname(request.nickname())
        .password(passwordEncoder.encode(request.password()))
        .createdAt(LocalDateTime.now())
        .role(UserRole.USER)
        .isDeleted(false)
        .build();

    User saved = userRepository.save(user);

    return new UserDto(
        saved.getId(),
        saved.getEmail(),
        saved.getNickname(),
        saved.getCreatedAt()
    );
  }
}
