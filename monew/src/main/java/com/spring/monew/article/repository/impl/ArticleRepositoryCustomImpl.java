package com.spring.monew.article.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.monew.article.controller.dto.response.ArticleDto;
import com.spring.monew.article.controller.dto.response.CursorPageResponseArticleDto;
import com.spring.monew.article.domain.ArticleSource;
import com.spring.monew.article.domain.QArticle;
import com.spring.monew.article.repository.ArticleRepositoryCustom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private static final QArticle article = QArticle.article;

  @Override
  public CursorPageResponseArticleDto findCursorPagedArticles(
      String keyword,
      List<String> interests,
      List<String> sources,
      Instant from,
      Instant to,
      String orderBy,
      String direction,
      String cursor,
      int limit,
      UUID userId) {

    BooleanBuilder builder = new BooleanBuilder();

    if (keyword != null && !keyword.isEmpty()) {
      builder.and(
          article.title.containsIgnoreCase(keyword)
              .or(article.summary.containsIgnoreCase(keyword))
      );
    }

    if (interests != null && !interests.isEmpty()) {
      builder.and(article.interest.name.in(interests));
    }

    if (sources != null && !sources.isEmpty()) {
      List<ArticleSource> sourceEnums = sources.stream()
          .map(ArticleSource::valueOf)
          .toList();
      builder.and(article.source.in(sourceEnums));
    }

    if (from != null) {
      builder.and(article.publishDate.goe(from));
    }

    if (to != null) {
      builder.and(article.publishDate.loe(to));
    }

    Long totalCount = queryFactory
        .select(article.count())
        .from(article)
        .where(builder)
        .fetchOne();

    applyCursorCondition(orderBy, direction, cursor, builder);

    OrderSpecifier<?> primaryOrder = getOrderSpecifier(orderBy, direction);
    OrderSpecifier<?> secondaryOrder = getCreatedAtOrderSpecifier(direction);
    OrderSpecifier<?> stabilityOrder = new OrderSpecifier<>(
        "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC,
        article.id
    );

    List<Tuple> tuples = queryFactory
        .select(
            article.id,
            article.source,
            article.sourceUrl,
            article.title,
            article.publishDate,
            article.summary,
            article.commentCount,
            article.viewCount,
            article.createdAt
        )
        .from(article)
        .where(builder)
        .orderBy(primaryOrder, secondaryOrder, stabilityOrder)
        .limit(limit + 1)
        .fetch();

    boolean hasNext = tuples.size() > limit;
    if (hasNext) {
      tuples.remove(limit);
    }

    List<ArticleDto> results = tuples.stream()
        .map(tuple -> new ArticleDto(
            tuple.get(article.id),
            tuple.get(article.source),
            tuple.get(article.sourceUrl),
            tuple.get(article.title),
            tuple.get(article.publishDate),
            tuple.get(article.summary),
            tuple.get(article.commentCount),
            tuple.get(article.viewCount),
            null
        ))
        .toList();

    String nextCursor = null;
    Instant nextAfter = null;
    if (hasNext && !tuples.isEmpty()) {
      Tuple lastTuple = tuples.get(tuples.size() - 1);
      nextCursor = lastTuple.get(article.id).toString();
      nextAfter = lastTuple.get(article.createdAt);
    }

    return new CursorPageResponseArticleDto(
        results,
        nextCursor,
        nextAfter,
        limit,
        totalCount != null ? totalCount : 0L,
        hasNext
    );
  }

  private void applyCursorCondition(String orderBy, String direction, String cursor,
      BooleanBuilder builder) {
    if (cursor == null) {
      return;
    }

    UUID cursorId;
    try {
      cursorId = UUID.fromString(cursor);
    } catch (IllegalArgumentException e) {
      return;
    }

    Tuple row = queryFactory
        .select(article.publishDate, article.viewCount, article.commentCount, article.createdAt)
        .from(article)
        .where(article.id.eq(cursorId))
        .fetchOne();

    if (row == null) {
      return;
    }

    Instant cursorPublishDate = row.get(article.publishDate);
    Long cursorViewCount = row.get(article.viewCount);
    Long cursorCommentCount = row.get(article.commentCount);
    Instant cursorCreatedAt = row.get(article.createdAt);

    boolean isAsc = "ASC".equalsIgnoreCase(direction);

    BooleanExpression byCreatedAtThenId = isAsc
        ? article.createdAt.gt(cursorCreatedAt)
        .or(article.createdAt.eq(cursorCreatedAt)
            .and(article.id.gt(cursorId)))
        : article.createdAt.lt(cursorCreatedAt)
            .or(article.createdAt.eq(cursorCreatedAt)
                .and(article.id.lt(cursorId)));

    switch (orderBy) {
      case "publishDate" -> builder.and(
          isAsc
              ? article.publishDate.gt(cursorPublishDate)
              .or(article.publishDate.eq(cursorPublishDate).and(byCreatedAtThenId))
              : article.publishDate.lt(cursorPublishDate)
                  .or(article.publishDate.eq(cursorPublishDate).and(byCreatedAtThenId))
      );

      case "viewCount" -> builder.and(
          isAsc
              ? article.viewCount.gt(cursorViewCount)
              .or(article.viewCount.eq(cursorViewCount).and(byCreatedAtThenId))
              : article.viewCount.lt(cursorViewCount)
                  .or(article.viewCount.eq(cursorViewCount).and(byCreatedAtThenId))
      );

      case "commentCount" -> builder.and(
          isAsc
              ? article.commentCount.gt(cursorCommentCount)
              .or(article.commentCount.eq(cursorCommentCount).and(byCreatedAtThenId))
              : article.commentCount.lt(cursorCommentCount)
                  .or(article.commentCount.eq(cursorCommentCount).and(byCreatedAtThenId))
      );

      default -> builder.and(byCreatedAtThenId);
    }
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    return switch (orderBy) {
      case "publishDate" -> new OrderSpecifier<>(order, article.publishDate);
      case "viewCount" -> new OrderSpecifier<>(order, article.viewCount);
      case "commentCount" -> new OrderSpecifier<>(order, article.commentCount);
      default -> new OrderSpecifier<>(order, article.createdAt);
    };
  }

  private OrderSpecifier<?> getCreatedAtOrderSpecifier(String direction) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    return new OrderSpecifier<>(order, article.createdAt);
  }
}
