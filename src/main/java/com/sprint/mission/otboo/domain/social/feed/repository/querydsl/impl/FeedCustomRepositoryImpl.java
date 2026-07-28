package com.sprint.mission.otboo.domain.social.feed.repository.querydsl.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.querydsl.FeedCustomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedCustomRepositoryImpl implements FeedCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public List<Feed> findFeeds(FeedListParams params) {
    return List.of();
  }
}