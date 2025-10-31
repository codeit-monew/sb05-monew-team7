package com.spring.monew.activity.service;

import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import java.util.UUID;

public interface UserActivityService {
  UserActivityDto getUserActivity(UUID userId);
}