package com.spring.monew.interest.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.monew.interest.controller.dto.response.CursorPageResponseInterestDto;
import com.spring.monew.interest.controller.dto.response.InterestDto;
import com.spring.monew.interest.controller.dto.response.QInterestDto;
import com.spring.monew.interest.domain.QInterest;
import com.spring.monew.interest.repository.InterestRepositoryCustom;
import com.spring.monew.subscription.domain.QSubscription;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryCustomImpl implements InterestRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private static final QInterest interest = QInterest.interest;
  private static final QSubscription subscription = QSubscription.subscription;

  @Override
  public CursorPageResponseInterestDto findCursorPagedInterests(String keyword, String orderBy,
      String direction, String cursor, Instant after, int limit, UUID userId) {

    BooleanBuilder builder = new BooleanBuilder();

    // 검색 조건 (keyword, interestName)
    if (keyword != null && !keyword.isEmpty()) {
      builder.and(
          interest.name.containsIgnoreCase(keyword)
              .or(interest.keywordsString.containsIgnoreCase(keyword))  // ✅ JSON 문자열에 LIKE
      );
    }

    BooleanExpression cursorCondition = buildCursorCondition(direction, cursor, after);
    if (cursorCondition != null) {
      builder.and(cursorCondition);
    }
    // 정렬 기준
    OrderSpecifier<?> primaryOrder = getOrderSpecifier(orderBy, direction);
    OrderSpecifier<?> secondaryOrder = getCreatedAtOrderSpecifier(direction);

    List<InterestDto> results = queryFactory
        .select(new QInterestDto(
            interest.id,
            interest.name,
            interest.keywords,
            interest.subscriptionsCount,
            subscription.id.isNotNull(), // 내가 구독 중인지 여부
            interest.createdAt
            )
        )
        .from(interest)
        .leftJoin(subscription)
        .on(subscription.interest.id.eq(interest.id)
            .and(subscription.user.id.eq(userId)))
        .where(builder)
        .orderBy(primaryOrder, secondaryOrder)
        .limit(limit + 1)
        .fetch();

    boolean hasNext = results.size() > limit;
    if (hasNext) results.remove(limit);

    // 커서 계산
    String nextCursor = hasNext ? results.get(results.size() - 1).id().toString() : null;
    Instant nextAfter = hasNext ? results.get(results.size() - 1).createdAt() : null;

    return new CursorPageResponseInterestDto(
        results,
        nextCursor,
        nextAfter,
        limit,
        results.size(),
        hasNext
    );
  }

  // 유틸 메서드

  private BooleanExpression buildCursorCondition(String direction, String cursor, Instant after) {
    if (cursor == null || after == null) return null;

    UUID cursorId = UUID.fromString(cursor);
    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    return isAsc
        ? interest.createdAt.after(after)
        .or(interest.createdAt.eq(after).and(interest.id.gt(cursorId)))
        : interest.createdAt.before(after)
            .or(interest.createdAt.eq(after).and(interest.id.lt(cursorId)));
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    return switch (orderBy) {
      case "subscriberCount" -> new OrderSpecifier<>(order, InterestRepositoryCustomImpl.interest.subscriptionsCount);
      case "name" -> new OrderSpecifier<>(order, InterestRepositoryCustomImpl.interest.name);
      default -> new OrderSpecifier<>(order, InterestRepositoryCustomImpl.interest.name);
    };
  }

  private OrderSpecifier<?> getCreatedAtOrderSpecifier(String direction) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    return new OrderSpecifier<>(order, interest.createdAt);
  }
}