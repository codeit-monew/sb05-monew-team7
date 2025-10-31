package com.spring.monew.batch.listener;

import com.spring.monew.article.domain.Article;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.service.NotificationService;
import com.spring.monew.subscription.repository.SubscriptionRepository;
import java.util.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleNotificationListener extends ItemListenerSupport<Article, Article>
    implements StepExecutionListener {

  private final SubscriptionRepository subscriptionRepository;
  private final NotificationService notificationService;

  private final Map<UUID, Agg> aggByInterest = new HashMap<>();

  private static final class Agg {
    final String interestName;
    int count;
    Agg(String interestName) { this.interestName = interestName; }
  }

  @Override
  public void beforeStep(@NonNull StepExecution stepExecution) {  // ✅ @NonNull
    aggByInterest.clear();
  }
  // Writer가 저장한 후 호출: 이번 청크에서 저장된 기사들을 집계
  @Override
  public void afterWrite(Chunk<? extends Article> items) {
    for (Article a : items) {
      UUID interestId = a.getInterest().getId();
      String interestName = a.getInterest().getName(); // Lazy면 Reader/Processor에서 보장 필요
      aggByInterest.compute(interestId, (k, v) -> {
        if (v == null) { v = new Agg(interestName); v.count = 1; }
        else { v.count += 1; }
        return v;
      });
    }
  }

  // 스텝 종료 시 한 번만 발송
  @Override
  public ExitStatus afterStep(@NonNull StepExecution stepExecution) { // @NonNull
    int sent = 0;
    for (var e : aggByInterest.entrySet()) {
      UUID interestId = e.getKey();
      Agg a = e.getValue();

      List<UUID> subscriberIds = subscriptionRepository.findUserIdsByInterestId(interestId);
      if (subscriberIds.isEmpty()) continue;

      String content = a.interestName + "와 관련된 기사가 " + a.count + "건 등록되었습니다.";
      notificationService.createForUsers(
          subscriberIds,
          content,
          NotificationResourceType.SUBSCRIPTION,
          interestId
      );
      sent++;
    }
    aggByInterest.clear();
    return stepExecution.getExitStatus();
  }
}
