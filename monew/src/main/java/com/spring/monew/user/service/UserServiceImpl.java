package com.spring.monew.user.service;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.controller.dto.request.UserUpdateRequest;
import com.spring.monew.user.domain.User;
import com.spring.monew.user.domain.UserRole;
import com.spring.monew.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
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

  /**
   * 사용자 논리 삭제
   * (is_deleted=true,deleted_at=시간으로 변경)
   */
  @Override
  @Transactional
  public void removeUserLogical(UUID userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

    // JPA가 자동으로 @SQLDelete 적용 -> 기존 DELETE 쿼리 대신 UPDATE 쿼리로 대체됨
    userRepository.delete(user);
  }

  /**
   * 사용자 논리 삭제
   * (하루 뒤 DB에서 완전한 삭제 )
   */
  @Override
  @Transactional
  public int removeUsersAfterOneDay() {
    Instant threshold = Instant.now().minus(Duration.ofDays(1));
    return userRepository.deletedSoftUsers(threshold);
  }
}