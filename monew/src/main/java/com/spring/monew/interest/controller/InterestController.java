package com.spring.monew.interest.controller;

import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.interest.controller.dto.request.InterestRegisterRequest;
import com.spring.monew.interest.controller.dto.request.InterestUpdateRequest;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.service.InterestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
@Tag(name = "관심사 관리", description = "관심사 관련 API")
public class InterestController {

  private final InterestService interestService;
  private final RequestUserExtractor userExtractor;

  @Operation(summary = "관심사 등록", description = "새로운 관심사를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "등록 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping
  public InterestDto interestAdd(@RequestBody InterestRegisterRequest registerRequest) {
    return interestService.addInterest(registerRequest);
  }

  @Operation(summary = "관심사 목록 조회", description = "조건에 맞는 관심사 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (정렬 기준 오류, 페이지네이션 파라미터 오류 등)"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @GetMapping
  public CursorPageResponseInterestDto interestList(
      @RequestParam(required = false) String keyword,               // 검색어 (nullable)
      @RequestParam(defaultValue = "name") String orderBy,          // 정렬 기준 (기본값 name)
      @RequestParam(defaultValue = "ASC") String direction,         // 정렬 방향 (기본값 ASC)
      @RequestParam(required = false) String cursor,                // 커서 값
      @RequestParam(required = false) Instant after,                // 보조 커서(createdAt)
      @RequestParam(defaultValue = "50") int limit,                 // 페이지 크기
      Principal principal
  ) {
    UUID userId = userExtractor.extractUserId(principal);
    return interestService.getInterests(keyword, orderBy, direction, cursor, after, limit, userId);
  }

  @Operation(summary = "관심사 정보 수정", description = "관심사의 키워드를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "수정 성공"),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (입력값 검증 실패)"),
      @ApiResponse(responseCode = "404", description = "관심사 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PatchMapping("/{interestId}")
  public InterestDto interestModify(@PathVariable UUID interestId,
      @RequestBody InterestUpdateRequest updateRequest) {
    return interestService.modifyInterest(interestId, updateRequest);
  }

  @Operation(summary = "관심사 물리 삭제", description = "관심사를 물리적으로 삭제합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "삭제 성공"),
      @ApiResponse(responseCode = "404", description = "관심사 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{interestId}")
  public void interestRemove(@PathVariable UUID interestId) {
    interestService.removeInterest(interestId);
  }
}
