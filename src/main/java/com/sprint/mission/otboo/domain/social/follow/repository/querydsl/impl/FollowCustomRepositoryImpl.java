package com.sprint.mission.otboo.domain.social.follow.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.social.follow.entity.QFollow.follow;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.repository.querydsl.FollowCustomRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowCustomRepositoryImpl implements FollowCustomRepository {

  private final JPAQueryFactory queryFactory;

  @Override
  public CursorPageResponse<Follow> findFollowings(FollowingListParams params) {
    List<Follow> raw = fetchFollowings(params);

    boolean hasNext = raw.size() > params.limit();
    List<Follow> page = hasNext ? raw.subList(0, params.limit()) : raw;

    CursorInfo cursorInfo = createCursorInfo(page, hasNext);

    return new CursorPageResponse<>(page, cursorInfo.nextCursor(), cursorInfo.nextIdAfter(),
        hasNext, countFollowings(params), "createdAt", SortDirection.DESCENDING);
  }

  private List<Follow> fetchFollowings(FollowingListParams params) {
    return queryFactory
        .selectFrom(follow)
        .where(follow.followerId.eq(params.followerId()))
        .orderBy(follow.createdAt.desc())
        .limit(params.limit() + 1L)
        .fetch();
  }

  private long countFollowings(FollowingListParams params) {
    return Optional.ofNullable(
        queryFactory.select(follow.count())
            .from(follow)
            .where(follow.followerId.eq(params.followerId()))
            .fetchOne()
    ).orElse(0L);
  }

  private CursorInfo createCursorInfo(List<Follow> page, boolean hasNext) {
    if (!hasNext || page.isEmpty()) {
      return new CursorInfo(null, null);
    }
    Follow last = page.get(page.size() - 1);
    return new CursorInfo(last.getCreatedAt().toString(), last.getId());
  }

  private record CursorInfo(String nextCursor, UUID nextIdAfter) {

  }
}