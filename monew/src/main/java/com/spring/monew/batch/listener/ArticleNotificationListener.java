package com.spring.monew.batch.listener;

import com.spring.monew.article.domain.Article;
import com.spring.monew.interest.domain.Interest;
import com.spring.monew.interest.repository.InterestRepository;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.service.NotificationService;
import com.spring.monew.subscription.repository.SubscriptionRepository;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleNotificationListener extends ItemListenerSupport<Article, Article>
    implements StepExecutionListener {

  private static final String COUNT_MAP_KEY = "aggByInterest";

  private final SubscriptionRepository subscriptionRepository;
  private final NotificationService notificationService;
  private final InterestRepository interestRepository;

  @Value("${monew.writer.optimize:false}")
  private boolean optimize; // 설정 주입

  // EC에 저장될 집계 구조
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static final class Agg implements Serializable {
    private String interestName;
    private int count;
    public Agg(int count) {
      this.count = count;
    }
  }
  @Override
  public void beforeStep(@NonNull StepExecution stepExecution) {
    // 최적화 모드라도 listener가 필요시 쓸 수 있도록 키만 초기화(타입은 afterWrite/afterStep에서 보장)
    if (!stepExecution.getExecutionContext().containsKey(COUNT_MAP_KEY)) {
      stepExecution.getExecutionContext().put(COUNT_MAP_KEY, new HashMap<>());
    }
  }

  @SuppressWarnings("unchecked")
  private Map<UUID, Agg> getOrInitAggMap() {
    var sync = StepSynchronizationManager.getContext();
    if (sync == null || sync.getStepExecution() == null) {
      log.debug("[알림] StepSynchronizationContext 미존재 - afterWrite 집계 스킵");
      return null;
    }
    var ec = sync.getStepExecution().getExecutionContext();
    Object obj = ec.get(COUNT_MAP_KEY);

    // writer 최적화 모드면 afterWrite는 스킵하므로 여기서 Map<UUID, Agg>를 사용할 일은 거의 없음
    if (obj instanceof Map) {
      try {
        return (Map<UUID, Agg>) obj; // 이미 Agg 맵이면 그대로
      } catch (ClassCastException ignore) {
        // 타입이 다르면 새로 초기화
      }
    }
    Map<UUID, Agg> map = new HashMap<>();
    ec.put(COUNT_MAP_KEY, map);
    return map;
  }

  @Override
  public void afterWrite(@NonNull Chunk<? extends Article> items) {
    //  최적화 모드에서는 writer가 EC에 Map<UUID,Integer>를 채우므로,
    //  listener afterWrite 집계는 충돌 방지를 위해 스킵한다.
    if (optimize) {
      log.debug("[알림] writer 최적화 모드 - afterWrite 집계 스킵");
      return;
    }

    Map<UUID, Agg> map = getOrInitAggMap();
    if (map == null) {
      return;
    }
    int candidates = 0, counted = 0;

    for (Article a : items) {
      if (a == null) continue;
      candidates++;

      UUID interestId = null;
      // FK 필드 직접 접근 우선 (lazy loading 회피)
      try {
        interestId = a.getInterestId();
      } catch (Exception ignore) { /* 안전장치 */ }
      if (interestId == null && a.getInterest() != null) {
        interestId = a.getInterest().getId();
      }
      if (interestId == null) continue;

      map.compute(interestId, (k, v) -> {
        if (v == null) return new Agg(1);
        v.count += 1;
        return v;
      });
      counted++;
    }
    log.debug("[알림] afterWrite 집계: 후보 {}건 중 {}건 카운팅(optimize={})", candidates, counted, optimize);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
    var ec = stepExecution.getExecutionContext();
    Object obj = ec.get(COUNT_MAP_KEY);

    // 아무 것도 없으면 종료
    if (!(obj instanceof Map) || ((Map<?, ?>) obj).isEmpty()) {
      log.debug("[알림] 이번 스텝에서 집계 없음(optimize={})", optimize);
      return stepExecution.getExitStatus();
    }

    // 두 케이스 모두 처리:
    // 1) optimize=false → Map<UUID, Agg>
    // 2) optimize=true  → Map<UUID, Integer>
    Map<UUID, Agg> aggMap = new HashMap<>();

    Map<?, ?> raw = (Map<?, ?>) obj;
    for (Map.Entry<?, ?> e : raw.entrySet()) {
      Object key = e.getKey();
      Object val = e.getValue();
      if (!(key instanceof UUID)) continue;

      UUID interestId = (UUID) key;
      if (val instanceof Agg) {
        aggMap.put(interestId, (Agg) val);
      } else if (val instanceof Integer) {
        Agg a = new Agg((Integer) val);
        aggMap.put(interestId, a);
      } else {
        log.warn("[알림] aggByInterest 값 타입 미지원: key={}, valueType={}", interestId, (val == null ? "null" : val.getClass()));
      }
    }

    if (aggMap.isEmpty()) {
      log.debug("[알림] 집계 변환 결과 없음(optimize={})", optimize);
      return stepExecution.getExitStatus();
    }

    // 이름 벌크 로딩
    Set<UUID> interestIds = aggMap.keySet();
    Map<UUID, String> names = interestRepository.findAllById(interestIds).stream()
        .collect(Collectors.toMap(Interest::getId, it -> {
          String name = it.getName();
          return (name == null || name.isBlank()) ? "관심사" : name;
        }));

    int kinds = aggMap.size();
    int sentBatches = 0;

    for (var entry : aggMap.entrySet()) {
      UUID interestId = entry.getKey();
      Agg agg = entry.getValue();
      agg.interestName = names.getOrDefault(interestId, "관심사");

      try {
        List<UUID> subscriberIds = subscriptionRepository.findUserIdsByInterestId(interestId);
        if (subscriberIds == null || subscriberIds.isEmpty()) {
          log.debug("[알림] 구독자 없음: interestId={}", interestId);
          continue;
        }

        String content = String.format("%s와 관련된 기사가 %d건 등록되었습니다.",
            agg.interestName, agg.count);

        notificationService.createForUsers(
            subscriberIds,
            content,
            NotificationResourceType.SUBSCRIPTION,
            interestId
        );
        sentBatches++;
      } catch (Exception ex) {
        log.warn("[알림] 관심사 알림 생성 실패: interestId={}, name={}, count={}",
            interestId, agg.interestName, agg.count, ex);
      }
    }

    log.info("[알림] 관심사별 배치 알림 생성 완료: 관심사 종류 {}개, 발송 {}건(optimize={})", kinds, sentBatches, optimize);
    return stepExecution.getExitStatus();
  }
}