package com.spring.monew.subscription.controller;

import com.spring.monew.activity.service.UserActivityService;
import com.spring.monew.common.util.RequestUserExtractor;
import com.spring.monew.notification.service.NotificationService;
import com.spring.monew.subscription.controller.dto.response.SubscriptionDto;
import com.spring.monew.subscription.domain.Subscription;
import com.spring.monew.subscription.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
@Tag(name = "관심사 구독 관리", description = "관심사 구독 관련 API")
public class SubscriptionController {
  private final SubscriptionService subscriptionService;
  private final RequestUserExtractor userExtractor;
  private final UserActivityService userActivityService;

  @Operation(summary = "관심사 등록", description = "관심사를 구독합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "구독 성공"),
      @ApiResponse(responseCode = "404", description = "관심사 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @PostMapping("/{interestId}/subscriptions")
  public SubscriptionDto subscriptionAdd(@PathVariable UUID interestId,
      Principal principal) {

    UUID userId = userExtractor.extractUserId(principal);
    Subscription subscription = subscriptionService.addSubscription(interestId, userId);

    userActivityService.addSubscriptionActivity(subscription);

    return new SubscriptionDto(
        subscription.getId(),
        subscription.getUser().getId(),
        subscription.getInterest().getName(),
        subscription.getInterest().getKeywords(),
        subscription.getInterest().getSubscriptionsCount(),
        subscription.getCreatedAt());
  }

  @Operation(summary = "관심사 구독 취소", description = "관심사를 구독을 취소합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "구독 취소 성공"),
      @ApiResponse(responseCode = "404", description = "관심사 정보 없음"),
      @ApiResponse(responseCode = "500", description = "서버 내부 오류")
  })
  @DeleteMapping("/{interestId}/subscriptions")
  public void subscriptionRemove(@PathVariable UUID interestId,
      Principal principal) {
    UUID userId = userExtractor.extractUserId(principal);
    Subscription subscription = subscriptionService.removeSubscription(interestId, userId);

    userActivityService.removeSubscriptionActivity(subscription.getId());
  }
}
