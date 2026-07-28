package com.sprint.mission.otboo.domain.social.feed.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.social.feed.entity.QFeed.feed;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.querydsl.FeedCustomRepository;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.util.List;
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
        .where(feed.softDeletable.deletedAt.isNull())
        .orderBy(
            new OrderSpecifier<>(order, feed.createdAt),
            new OrderSpecifier<>(order, feed.id)
        )
        .limit(params.limit() + 1L)
        .fetch();
  }
}