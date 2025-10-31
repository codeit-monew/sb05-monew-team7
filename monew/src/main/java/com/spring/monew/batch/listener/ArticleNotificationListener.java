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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.listener.ItemListenerSupport;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.Chunk;
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
  private final InterestRepository interestRepository; // ★ 이름 벌크 조회용

  // EC에 저장될 집계 구조
  public static final class Agg implements Serializable {
    public int count;
    public String interestName; // afterStep에서 채움
    public Agg() {}
    public Agg(int count) { this.count = count; }
  }

  @Override
  public void beforeStep(@NonNull StepExecution stepExecution) {
    stepExecution.getExecutionContext().put(COUNT_MAP_KEY, new HashMap<UUID, Agg>());
  }

  @SuppressWarnings("unchecked")
  private Map<UUID, Agg> getAggMap() {
    return (Map<UUID, Agg>) StepSynchronizationManager.getContext()
        .getStepExecution().getExecutionContext()
        .get(COUNT_MAP_KEY);
  }

  @Override
  public void afterWrite(@NonNull Chunk<? extends Article> items) {
    Map<UUID, Agg> map = getAggMap();
    if (map == null) {
      log.warn("[알림] 집계 맵 누락 - 스킵");
      return;
    }

    int candidates = 0, counted = 0;
    for (Article a : items) {
      if (a == null) continue;
      candidates++;

      // 핵심: 연관 ID 우선, 없으면 읽기전용 FK 보조
      UUID interestId = null;
      if (a.getInterest() != null) {
        // Hibernate 프록시여도 getId()는 초기화 없이 안전
        interestId = a.getInterest().getId();
      }
      if (interestId == null) {
        interestId = a.getInterestId();
      }
      if (interestId == null) continue; // 집계 불가

      map.compute(interestId, (k, v) -> {
        if (v == null) return new Agg(1);
        v.count += 1;
        return v;
      });
      counted++;
    }
    log.debug("[알림] afterWrite 집계: 후보 {}건 중 {}건 카운팅", candidates, counted);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
    // Writer가 EC에 Map<UUID,Integer> 형태로 누적해 둔 경우를 흡수/정규화
    Object raw = stepExecution.getExecutionContext().get(COUNT_MAP_KEY);
    Map<UUID, Agg> map;

    if (raw == null) {
      log.debug("[알림] 이번 스텝에서 집계 없음(EC 비어있음)");
      return stepExecution.getExitStatus();
    }

    if (raw instanceof Map<?, ?> anyMap) {
      if (anyMap.isEmpty()) {
        // 비어 있어도 타입을 Agg로 확정해 둠
        map = new HashMap<>();
        stepExecution.getExecutionContext().put(COUNT_MAP_KEY, map);
      } else {
        Object sampleVal = anyMap.values().iterator().next();
        if (sampleVal instanceof Agg) {
          // 이미 리스너 집계(Map<UUID, Agg>) 형태
          @SuppressWarnings("unchecked")
          Map<UUID, Agg> casted = (Map<UUID, Agg>) anyMap;
          map = casted;
        } else if (sampleVal instanceof Integer) {
          // Writer 집계(Map<UUID, Integer>)를 Agg로 변환하여 대체
          Map<UUID, Agg> converted = new HashMap<>();
          for (Map.Entry<?, ?> e : anyMap.entrySet()) {
            Object k = e.getKey();
            Object v = e.getValue();
            if (k instanceof UUID key && v instanceof Integer cnt) {
              converted.put(key, new Agg(cnt));
            }
          }
          // EC에 Agg 맵으로 치환
          stepExecution.getExecutionContext().put(COUNT_MAP_KEY, converted);
          map = converted;
          log.debug("[알림] Writer 집계를 Agg로 정규화: {}개", map.size());
        } else {
          log.warn("[알림] 지원하지 않는 집계 타입: {}", sampleVal.getClass().getName());
          return stepExecution.getExitStatus();
        }
      }
    } else {
      log.warn("[알림] EC '{}' 값이 Map이 아님: {}", COUNT_MAP_KEY, raw.getClass().getName());
      return stepExecution.getExitStatus();
    }

    if (map.isEmpty()) {
      log.debug("[알림] 이번 스텝에서 집계 없음");
      return stepExecution.getExitStatus();
    }

    // 관심사 이름 벌크 로딩(지연로딩 금지, 한 번에)
    Set<UUID> interestIds = map.keySet();
    Map<UUID, String> names = interestRepository.findAllById(interestIds).stream()
        .collect(Collectors.toMap(Interest::getId, it -> {
          String name = it.getName();
          return (name == null || name.isBlank()) ? "관심사" : name;
        }));

    int kinds = map.size();
    int sentBatches = 0;

    for (Map.Entry<UUID, Agg> e : map.entrySet()) {
      UUID interestId = e.getKey();
      Agg agg = e.getValue();
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

    log.info("[알림] 관심사별 배치 알림 생성 완료: 관심사 종류 {}개, 발송 {}건", kinds, sentBatches);
    return stepExecution.getExitStatus();
  }
}