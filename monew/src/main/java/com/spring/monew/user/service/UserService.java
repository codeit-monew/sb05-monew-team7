package com.spring.monew.user.service;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.domain.UserRole;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    User user = new User(
        request.email(),
        request.nickname(),
        passwordEncoder.encode(request.password()),
        Instant.now(),
        UserRole.USER
    );

    User saved = userRepository.save(user);

    return new UserDto(
        saved.getId(),
        saved.getEmail(),
        saved.getNickname(),
        saved.getCreatedAt()

    );
  }
}
