package com.sprint.mission.otboo.domain.social.feed.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.social.feed.entity.QFeed.feed;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.querydsl.FeedCustomRepository;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Feed> findFeeds(FeedListParams params) {
    Order order = params.sortDirection() == SortDirection.ASCENDING ? Order.ASC : Order.DESC;

    return queryFactory
        .selectFrom(feed)
        .where(
            feed.softDeletable.deletedAt.isNull(),
            eqAuthorId(params.authorIdEqual()),
            cursorCondition(params)
        )
        .orderBy(
            new OrderSpecifier<>(order, feed.createdAt),
            new OrderSpecifier<>(order, feed.id)
        )
        .limit(params.limit() + 1L)
        .fetch();
  }

  private BooleanExpression eqAuthorId(UUID authorId) {
    return authorId == null ? null : feed.authorId.eq(authorId);
  }

  private BooleanExpression cursorCondition(FeedListParams params) {
    if (params.cursor() == null) {
      return null;
    }
    boolean isAsc = params.sortDirection() == SortDirection.ASCENDING;
    Instant cursorCreatedAt = Instant.parse(params.cursor());
    UUID cursorId = params.idAfter();

    return isAsc ? feed.createdAt.gt(cursorCreatedAt)
        .or(feed.createdAt.eq(cursorCreatedAt).and(feed.id.gt(cursorId)))
        : feed.createdAt.lt(cursorCreatedAt)
            .or(feed.createdAt.eq(cursorCreatedAt).and(feed.id.lt(cursorId)));
  }
}