package com.sprint.mission.otboo.domain.social.feed.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.social.feed.entity.QFeed.feed;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSortBy;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.querydsl.FeedCustomRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<Feed> findFeeds(FeedListParams params) {
    List<Feed> raw = queryFactory
        .selectFrom(feed)
        .where(
            feed.softDeletable.deletedAt.isNull(),
            eqAuthorId(params.authorIdEqual()),
            containsKeyword(params.keywordLike()),
            cursorCondition(params)
        )
        .orderBy(
            sortOrderSpecifier(params.sortBy(), params.sortDirection()),
            idOrderSpecifier(params.sortDirection())
        )
        .limit(params.limit() + 1L)
        .fetch();

    boolean hasNext = raw.size() > params.limit();
    List<Feed> page = hasNext ? raw.subList(0, params.limit()) : raw;

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !page.isEmpty()) {
      Feed last = page.get(page.size() - 1);
      nextCursor = extractCursor(last, params.sortBy());
      nextIdAfter = last.getId();
    }

    long totalCount = Optional.ofNullable(
        queryFactory.select(feed.count()).from(feed)
            .where(
                feed.softDeletable.deletedAt.isNull(),
                eqAuthorId(params.authorIdEqual()),
                containsKeyword(params.keywordLike())
            )
            .fetchOne()
    ).orElse(0L);

    return new CursorPageResponse<>(
        page, nextCursor, nextIdAfter, hasNext, totalCount,
        params.sortBy().param(), params.sortDirection());
  }

  private String extractCursor(Feed last, FeedSortBy sortBy) {
    return switch (sortBy) {
      case CREATED_AT -> last.getCreatedAt().toString();
      case LIKE_COUNT -> String.valueOf(last.getLikeCount());
    };
  }

  private BooleanExpression eqAuthorId(UUID authorId) {
    return authorId == null ? null : feed.authorId.eq(authorId);
  }

  private BooleanExpression containsKeyword(String keyword) {
    return keyword == null ? null : feed.content.containsIgnoreCase(keyword);
  }

  private BooleanExpression cursorCondition(FeedListParams params) {
    if (params.cursor() == null) {
      return null;
    }
    boolean isAsc = params.sortDirection() == SortDirection.ASCENDING;
    UUID cursorId = params.idAfter();

    return switch (params.sortBy()) {
      case CREATED_AT -> {
        Instant value = Instant.parse(params.cursor());
        yield isAsc
            ? feed.createdAt.gt(value).or(feed.createdAt.eq(value).and(feed.id.gt(cursorId)))
            : feed.createdAt.lt(value).or(feed.createdAt.eq(value).and(feed.id.lt(cursorId)));
      }
      case LIKE_COUNT -> {
        long value = Long.parseLong(params.cursor());
        yield isAsc
            ? feed.likeCount.gt(value).or(feed.likeCount.eq(value).and(feed.id.gt(cursorId)))
            : feed.likeCount.lt(value).or(feed.likeCount.eq(value).and(feed.id.lt(cursorId)));
      }
    };
  }

  private OrderSpecifier<?> sortOrderSpecifier(FeedSortBy sortBy, SortDirection direction) {
    Order order = direction == SortDirection.ASCENDING ? Order.ASC : Order.DESC;
    return switch (sortBy) {
      case CREATED_AT -> new OrderSpecifier<>(order, feed.createdAt);
      case LIKE_COUNT -> new OrderSpecifier<>(order, feed.likeCount);
    };
  }

  private OrderSpecifier<?> idOrderSpecifier(SortDirection direction) {
    Order order = direction == SortDirection.ASCENDING ? Order.ASC : Order.DESC;
    return new OrderSpecifier<>(order, feed.id);
  }
}