package com.spring.monew.activity.controller;

import com.spring.monew.activity.controller.dto.response.UserActivityDto;
import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.common.util.RequestUserExtractor;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "사용자 활동 내역 관리", description = "사용자 활동 내역 관련 API")
@RestController
@RequiredArgsConstructor
@Validated
public class UserActivityController {

  private final UserActivityService userActivityService;
  private final RequestUserExtractor userExtractor;

  // 1) 특정 사용자 활동내역
  @Operation(summary = "사용자 활동 내역 조회", description = "사용자 ID로 활동 내역을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "사용자 활동 내역 조회 성공"),
      @ApiResponse(responseCode = "404", description = "사용자 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping("/api/user-activities/{userId}")
  public ResponseEntity<UserActivityDto> userActivityById(
      @PathVariable UUID userId,
      Principal principal
  ) {
    // 헤더가 없으면 null을 반환하도록 구현되어 있어야 합니다.
    UUID reqUserId = userExtractor.extractUserId(principal);
    // 헤더가 있을 때만 소유자 검증
    if (reqUserId != null && !reqUserId.equals(userId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "다른 사용자의 활동 내역을 조회할 수 없습니다."
      );
    }
    return ResponseEntity.ok(userActivityService.getUserActivity(userId));
  }
}