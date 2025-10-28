package com.spring.monew.comment.repository.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.spring.monew.comment.controller.dto.response.CommentDto;
import com.spring.monew.comment.controller.dto.response.CursorPageResponseCommentDto;
import com.spring.monew.comment.controller.dto.response.QCommentDto;
import com.spring.monew.comment.domain.QComment;
import com.spring.monew.comment.repository.CommentRepositoryCustom;
import com.spring.monew.commentlike.domain.QCommentLike;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;
  private static final QComment comment = QComment.comment;
  private static final QCommentLike commentLike = QCommentLike.commentLike;

  public CursorPageResponseCommentDto findCursorPagedComments(UUID articleId, String orderBy,
      String direction, String cursor, Instant after, int limit, UUID userId) {

    BooleanBuilder builder = new BooleanBuilder();

    builder.and(comment.article.id.eq(articleId));
    builder.and(comment.isDeleted.eq(false));

    BooleanExpression cursorCondition = buildCursorCondition(direction, cursor, after);
    if (cursorCondition != null) {
      builder.and(cursorCondition);
    }
    // 정렬 기준
    OrderSpecifier<?> primaryOrder = getOrderSpecifier(orderBy, direction);
    OrderSpecifier<?> secondaryOrder = getCreatedAtOrderSpecifier(direction);

    List<CommentDto> results = queryFactory
        .select(new QCommentDto(
                comment.id,
            comment.article.id,
            comment.user.id,
            comment.user.nickname,
            comment.content,
            comment.likeCount,
            commentLike.id.isNotNull(),
            comment.createdAt
            )
        )
        .from(comment)
        .leftJoin(commentLike)
        .on(commentLike.comment.id.eq(comment.id)
            .and(commentLike.user.id.eq(userId)))
        .where(builder)
        .orderBy(primaryOrder, secondaryOrder)
        .limit(limit + 1)
        .fetch();

    boolean hasNext = results.size() > limit;
    if (hasNext)
      results.remove(limit);

    // 커서 계산
    String nextCursor = hasNext ? results.get(results.size() - 1).id().toString() : null;
    Instant nextAfter = hasNext ? results.get(results.size() - 1).createdAt() : null;

    return new CursorPageResponseCommentDto(
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
        ? comment.createdAt.after(after)
        .or(comment.createdAt.eq(after).and(comment.id.gt(cursorId)))
        : comment.createdAt.before(after)
            .or(comment.createdAt.eq(after).and(comment.id.lt(cursorId)));
  }

  private OrderSpecifier<?> getOrderSpecifier(String orderBy, String direction) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    return switch (orderBy) {
      case "likeCount" -> new OrderSpecifier<>(order, CommentRepositoryCustomImpl.comment.likeCount);
      case "createdAt" -> new OrderSpecifier<>(order, CommentRepositoryCustomImpl.comment.createdAt);
      default -> new OrderSpecifier<>(order, CommentRepositoryCustomImpl.comment.createdAt);
    };
  }

  private OrderSpecifier<?> getCreatedAtOrderSpecifier(String direction) {
    Order order = "DESC".equalsIgnoreCase(direction) ? Order.DESC : Order.ASC;
    return new OrderSpecifier<>(order, comment.createdAt);
  }
}