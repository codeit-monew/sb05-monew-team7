package com.spring.monew.user.service;

import com.spring.monew.user.controller.dto.data.UserDto;
import com.spring.monew.user.controller.dto.request.UserRegisterRequest;
import com.spring.monew.user.controller.dto.request.UserUpdateRequest;
import java.util.UUID;

public interface UserService {

  //사용자 회원가입
  UserDto addUser(UserRegisterRequest request);

  //사용자 수정
  UserDto modifyUser(UUID userId, UserUpdateRequest request);
}

