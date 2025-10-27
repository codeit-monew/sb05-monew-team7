package com.spring.monew.notification.service;

import com.spring.monew.notification.domain.Notification;
import com.spring.monew.notification.domain.NotificationResourceType;
import com.spring.monew.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private static final int CONTENT_MAX = 255;

  private final NotificationRepository notificationRepository;

  @Transactional
  public UUID create(UUID userId, String content,
      NotificationResourceType type, UUID resourceId) {
    Notification n = Notification.of(userId, safeContent(content), type, resourceId);
    return notificationRepository.save(n).getId();
  }

  @Transactional
  public int createBulk(Collection<UUID> userIds, String content,
      NotificationResourceType type, UUID resourceId) {
    if (userIds == null || userIds.isEmpty()) return 0;

    final String msg = safeContent(content);

    List<Notification> batch = userIds.stream()
        .filter(Objects::nonNull)
        .distinct()
        .map(uid -> Notification.of(uid, msg, type, resourceId))
        .collect(Collectors.toList());

    if (batch.isEmpty()) return 0;

    notificationRepository.saveAll(batch); // 필요 시 saveAllAndFlush(batch)
    return batch.size();
  }

  private static String safeContent(String content) {
    if (content == null) return "";
    if (content.length() <= CONTENT_MAX) return content;
    return content.substring(0, CONTENT_MAX - 1) + "…";
  }

  // 관심사 기사 등록 → 구독자 전원 알림 생성
  @Transactional
  public int createInterestArticleNotifications(
      UUID interestId, String interestName, Collection<UUID> subscriberIds, long articleCount) {

    if (subscriberIds == null || subscriberIds.isEmpty()) return 0;
    String msg = String.format(Locale.ROOT,
        "[%s]와 관련된 기사가 %d건 등록되었습니다.", interestName, articleCount);

    return createBulk(subscriberIds, msg, NotificationResourceType.INTEREST, interestId);
  }

  // 내 댓글 좋아요 → 댓글 작성자(본인 제외)에게 알림 생성
  @Transactional
  public boolean createCommentLikeNotification(
      UUID commentId, UUID commentAuthorId, UUID likerUserId, String likerNickname) {

    if (commentAuthorId == null || likerUserId == null) return false;
    if (commentAuthorId.equals(likerUserId)) return false;

    String msg = String.format(Locale.ROOT,
        "[%s]님이 나의 댓글을 좋아합니다.", likerNickname);

    create(commentAuthorId, msg, NotificationResourceType.COMMENT, commentId);
    return true;
  }
}