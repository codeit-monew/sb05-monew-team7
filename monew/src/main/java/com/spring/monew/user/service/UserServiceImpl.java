package com.spring.monew.user.service;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.controller.dto.request.UserUpdateRequest;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.domain.UserRole;
import com.spring.monew.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

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

  /**
   * 사용자 정보 수정 (닉네임 변경)
   */
  @Transactional
  public UserDto modifyUser(UUID userId, UserUpdateRequest request) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    user.updateNickname(request.nickname());

    return new UserDto(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getCreatedAt()
    );
  }
}

