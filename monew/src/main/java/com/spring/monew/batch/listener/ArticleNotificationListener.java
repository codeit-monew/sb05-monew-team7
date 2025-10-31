package com.spring.monew.batch.listener;

import com.spring.monew.article.domain.Article;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.service.NotificationService;
import com.spring.monew.subscription.repository.SubscriptionRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.item.Chunk;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleNotificationListener extends ItemListenerSupport<Article, Article>
    implements StepExecutionListener {

  private final SubscriptionRepository subscriptionRepository;
  private final InterestRepository interestRepository;
  private final NotificationService notificationService;

  // interestId 별 기사 건수 집계 (afterWrite에서 id만 사용: LAZY 안전)
  private final Map<UUID, Integer> countByInterest = new HashMap<>();

  @Override
  public void beforeStep(@NonNull StepExecution stepExecution) {
    countByInterest.clear();
  }

  // Writer 커밋 후 호출 — 지연 로딩 접근 금지(식별자만)
  @Override
  public void afterWrite(Chunk<? extends Article> items) {
    for (Article a : items) {
      UUID interestId = (a.getInterest() != null) ? a.getInterest().getId() : null;
      if (interestId == null) continue;
      countByInterest.merge(interestId, 1, Integer::sum);
    }
  }

  // 스텝 종료 시 한 번만 알림 발송
  @Override
  public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
    if (countByInterest.isEmpty()) {
      return stepExecution.getExitStatus();
    }

    // 1) 관심사 이름 일괄 조회(N+1 방지)
    List<UUID> interestIds = new ArrayList<>(countByInterest.keySet());
    Map<UUID, String> nameByInterest = new HashMap<>();
    for (Interest it : interestRepository.findAllById(interestIds)) {
      nameByInterest.put(it.getId(), it.getName());
    }

    int topicSent = 0;
    int topicFailed = 0;

    // 2) 관심사별 구독자 조회 → 알림 발송 (실패는 로그만, 배치 실패로 전파하지 않음)
    for (UUID interestId : interestIds) {
      int cnt = countByInterest.getOrDefault(interestId, 0);
      if (cnt <= 0) continue;

      List<UUID> subscriberIds = subscriptionRepository.findUserIdsByInterestId(interestId);
      if (subscriberIds == null || subscriberIds.isEmpty()) continue;

      String interestName = nameByInterest.getOrDefault(interestId, "관심사");
      String content = interestName + "와 관련된 기사가 " + cnt + "건 등록되었습니다.";

      try {
        notificationService.createForUsers(
            subscriberIds,
            content,
            NotificationResourceType.SUBSCRIPTION, // FE 직렬화: "interest"
            interestId
        );
        topicSent++;
      } catch (Exception e) {
        topicFailed++;
        log.error(
            "[ArticleNotificationListener] Failed to send notification: interestId={}, interestName={}, count={}, subscribers={}",
            interestId, interestName, cnt, subscriberIds.size(), e
        );
        // 부가 기능 실패이므로 배치 스텝은 실패 처리하지 않음
      }
    }

    log.info(
        "[ArticleNotificationListener] notification topics sent={}, failed={}, interests={}",
        topicSent, topicFailed, countByInterest.size()
    );

    countByInterest.clear();
    return stepExecution.getExitStatus();
  }
}