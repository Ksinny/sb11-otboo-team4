package com.sprint.mission.otboo.domain.social.follow.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.authuser.user.entity.QUser.user;
import static com.sprint.mission.otboo.domain.social.follow.entity.QFollow.follow;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.mapper.FollowMapper;
import com.sprint.mission.otboo.domain.social.follow.repository.querydsl.FollowCustomRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FollowCustomRepositoryImpl implements FollowCustomRepository {

  private final JPAQueryFactory queryFactory;
  private final UserSummaryQueryRepository userSummaryQueryRepository;
  private final FollowMapper followMapper;

  @Override
  public CursorPageResponse<FollowDto> findFollowings(FollowingListParams params) {
    List<Follow> raw = fetchFollowings(params);

    boolean hasNext = raw.size() > params.limit();
    List<Follow> page = hasNext ? raw.subList(0, params.limit()) : raw;

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !page.isEmpty()) {
      Follow last = page.get(page.size() - 1);
      nextCursor = last.getCreatedAt().toString();
      nextIdAfter = last.getId();
    }

    List<FollowDto> data = toFollowDtos(page, params.followerId());

    return new CursorPageResponse<>(data, nextCursor, nextIdAfter, hasNext, countFollowings(params),
        "createdAt", SortDirection.DESCENDING
    );
  }

  private List<FollowDto> toFollowDtos(List<Follow> page, UUID followerId) {
    if (page.isEmpty()) {
      return List.of();
    }
    UserSummary follower = userSummaryQueryRepository.findByUserId(followerId);
    Map<UUID, UserSummary> followeeMap = userSummaryQueryRepository.findByUserIds(
            page.stream().map(Follow::getFolloweeId).toList()
        ).stream()
        .collect(Collectors.toMap(UserSummary::userId, Function.identity()));

    return page.stream()
        .map(f -> followMapper.toDto(f, follower, followeeMap.get(f.getFolloweeId())))
        .toList();
  }

  private List<Follow> fetchFollowings(FollowingListParams params) {
    JPAQuery<Follow> query = queryFactory.selectFrom(follow);
    if (params.nameLike() != null) {
      query.join(user).on(user.id.eq(follow.followeeId));
    }
    return query
        .where(
            follow.followerId.eq(params.followerId()),
            containsName(params.nameLike()),
            cursorCondition(params)
        )
        .orderBy(follow.createdAt.desc(), follow.id.desc())
        .limit(params.limit() + 1L)
        .fetch();
  }

  private long countFollowings(FollowingListParams params) {
    JPAQuery<Long> query = queryFactory.select(follow.count()).from(follow);
    if (params.nameLike() != null) {
      query.join(user).on(user.id.eq(follow.followeeId));
    }
    return Optional.ofNullable(
        query.where(
            follow.followerId.eq(params.followerId()),
            containsName(params.nameLike())
        ).fetchOne()
    ).orElse(0L);
  }

  private BooleanExpression containsName(String name) {
    return name == null ? null : user.name.containsIgnoreCase(name);
  }

  private BooleanExpression cursorCondition(FollowingListParams params) {
    if (params.cursor() == null) {
      return null;
    }
    Instant cursor = Instant.parse(params.cursor());
    UUID idAfter = params.idAfter();
    return follow.createdAt.lt(cursor)
        .or(follow.createdAt.eq(cursor).and(follow.id.lt(idAfter)));
  }
}