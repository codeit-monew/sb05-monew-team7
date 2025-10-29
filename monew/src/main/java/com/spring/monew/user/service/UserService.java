package com.spring.monew.user.service;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.controller.dto.request.UserUpdateRequest;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {

  //사용자 회원가입
  UserDto addUser(UserRegisterRequest request);

  //사용자 수정
  UserDto modifyUser(UUID userId, UserUpdateRequest request);

  //사용자 논리 삭제 (is_deleted=true,deleted_at=시간으로 상태 변경)
  void removeUserLogical(UUID userId);

  //사용자 논리 삭제 (1일 지난 유저 아예 삭제)
  int removeUsersAfterOneDay();
}

