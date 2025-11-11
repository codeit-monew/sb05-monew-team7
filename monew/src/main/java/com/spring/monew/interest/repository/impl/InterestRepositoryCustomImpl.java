package com.spring.monew.interest.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
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
  public CursorPageResponseInterestDto findCursorPagedInterests(
      String keyword, String orderBy, String direction,
      String cursor, Instant after, int limit, UUID userId) {

    BooleanBuilder builder = new BooleanBuilder();

    if (keyword != null && !keyword.isEmpty()) {
      builder.and(
          interest.name.containsIgnoreCase(keyword)
              .or(interest.keywordsString.containsIgnoreCase(keyword))
      );
    }

    // 커서 조건
    applyCursorCondition(orderBy, direction, cursor, builder);

    BooleanExpression subscribedExpression = JPAExpressions
        .selectOne()
        .from(subscription)
        .where(subscription.interest.id.eq(interest.id)
            .and(subscription.user.id.eq(userId)))
        .exists();

    // 정렬 옵션
    OrderSpecifier<?> primaryOrder = getOrderSpecifier(orderBy, direction);
    OrderSpecifier<?> secondaryOrder = getCreatedAtOrderSpecifier(direction);
    OrderSpecifier<?> stabilityOrder = new OrderSpecifier<>(
        "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC,
        interest.id
    );

    List<InterestDto> results = queryFactory
        .select(new QInterestDto(
            interest.id,
            interest.name,
            interest.keywords,
            interest.subscriptionsCount,
            subscribedExpression,
            interest.createdAt
        ))
        .from(interest)
        .where(builder)
        .orderBy(primaryOrder, secondaryOrder, stabilityOrder)
        .limit(limit + 1)
        .fetch();

    boolean hasNext = results.size() > limit;
    if (hasNext) {
      results.remove(limit);
    }

    String nextCursor = hasNext ? results.get(results.size() - 1).name() : null;
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

  //유틸 메서드

  private void applyCursorCondition(String orderBy, String direction, String cursor,
      BooleanBuilder builder) {
    if (cursor == null || cursor.isEmpty()) {
      return;
    }

    // tie-breaker
    Tuple row = queryFactory
        .select(interest.subscriptionsCount, interest.createdAt)
        .from(interest)
        .where(interest.name.eq(cursor))
        .fetchOne();

    if (row == null) {
      return;
    }

    Long cursorSubs = row.get(interest.subscriptionsCount);
    Instant cursorCreatedAt = row.get(interest.createdAt);

    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    // tie-breaker: createdAt → id
    BooleanExpression byCreatedAtThenId = isAsc
        ? interest.createdAt.gt(cursorCreatedAt)
        .or(interest.createdAt.eq(cursorCreatedAt)
            .and(interest.id.gt(interest.id)))
        : interest.createdAt.lt(cursorCreatedAt)
            .or(interest.createdAt.eq(cursorCreatedAt)
                .and(interest.id.lt(interest.id)));

    // primary 정렬 기준에 맞춘 cursor 처리
    if (orderBy.equals("subscriberCount")) {
      builder.and(
          isAsc
              ? interest.subscriptionsCount.gt(cursorSubs)
              .or(interest.subscriptionsCount.eq(cursorSubs).and(byCreatedAtThenId))
              : interest.subscriptionsCount.lt(cursorSubs)
                  .or(interest.subscriptionsCount.eq(cursorSubs).and(byCreatedAtThenId))
      );
    } else {
      builder.and(
          isAsc
              ? interest.name.gt(cursor)
              .or(interest.name.eq(cursor).and(byCreatedAtThenId))
              : interest.name.lt(cursor)
                  .or(interest.name.eq(cursor).and(byCreatedAtThenId))
      );
    }
  }

  @Override
  public List<String> findSimilarNames(String name, double threshold) {
    return queryFactory
        .select(interest.name)
        .from(interest)
        .where(
            interest.name.ne(name)
                .and(Expressions.booleanTemplate(
                    "similarity({0}, {1}) > {2}",
                    interest.name,
                    name,
                    threshold
                ))
        )
        .orderBy(Expressions.numberTemplate(
            Double.class, "similarity({0}, {1})", interest.name, name).desc())
        .limit(20)
        .fetch();
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    if ("subscriberCount".equals(orderBy)) {
      return new OrderSpecifier<>(order, InterestRepositoryCustomImpl.interest.subscriptionsCount);
    }
    return new OrderSpecifier<>(order, InterestRepositoryCustomImpl.interest.name);
  }

  private OrderSpecifier<?> getCreatedAtOrderSpecifier(String direction) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    return new OrderSpecifier<>(order, interest.createdAt);
  }
}