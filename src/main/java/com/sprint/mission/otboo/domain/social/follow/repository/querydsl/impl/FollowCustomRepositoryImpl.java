package com.sprint.mission.otboo.domain.social.follow.repository.querydsl.impl;

import static com.sprint.mission.otboo.domain.authuser.user.entity.QUser.user;
import static com.sprint.mission.otboo.domain.social.follow.entity.QFollow.follow;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowerListParams;
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
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class FollowCustomRepositoryImpl implements FollowCustomRepository {

  private final JPAQueryFactory queryFactory;
  private final UserSummaryQueryRepository userSummaryQueryRepository;
  private final FollowMapper followMapper;

  @Override
  public CursorPageResponse<FollowDto> findFollowings(FollowingListParams params) {
    return findFollows(params.followerId(), params.cursor(), params.idAfter(),
        params.limit(), params.nameLike(), Direction.FOLLOWINGS);
  }

  @Override
  public CursorPageResponse<FollowDto> findFollowers(FollowerListParams params) {
    return findFollows(params.followeeId(), params.cursor(), params.idAfter(),
        params.limit(), params.nameLike(), Direction.FOLLOWERS);
  }

  private CursorPageResponse<FollowDto> findFollows(
      UUID baseId, String cursor, UUID idAfter, int limit, String nameLike, Direction direction) {
    List<Follow> raw = fetchFollows(baseId, cursor, idAfter, limit, nameLike, direction);

    boolean hasNext = raw.size() > limit;
    List<Follow> page = hasNext ? raw.subList(0, limit) : raw;

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !page.isEmpty()) {
      Follow last = page.get(page.size() - 1);
      nextCursor = last.getCreatedAt().toString();
      nextIdAfter = last.getId();
    }

    return new CursorPageResponse<>(
        toFollowDtos(page, baseId, direction),
        nextCursor,
        nextIdAfter,
        hasNext,
        countFollows(baseId, nameLike, direction),
        "createdAt",
        SortDirection.DESCENDING
    );
  }

  private List<FollowDto> toFollowDtos(List<Follow> follows, UUID baseId, Direction direction) {
    if (follows.isEmpty()) {
      return List.of();
    }

    UserSummary baseSummary = userSummaryQueryRepository.findByUserId(baseId);

    Map<UUID, UserSummary> otherSummaryMap =
        userSummaryQueryRepository.findByUserIds(
                follows.stream().map(direction::otherSideId).toList()
            ).stream()
            .collect(Collectors.toMap(UserSummary::userId, Function.identity()));

    return follows.stream()
        .map(followEntity -> direction.toDto(
            followMapper, followEntity, baseSummary,
            otherSummaryMap.get(direction.otherSideId(followEntity))))
        .toList();
  }

  private List<Follow> fetchFollows(
      UUID baseId, String cursor, UUID idAfter, int limit, String nameLike, Direction direction) {
    JPAQuery<Follow> query = queryFactory.selectFrom(follow);

    if (StringUtils.hasText(nameLike)) {
      query.join(user).on(direction.joinCondition());
    }

    return query
        .where(
            direction.baseCondition(baseId),
            userNameContains(nameLike),
            cursorCondition(cursor, idAfter)
        )
        .orderBy(follow.createdAt.desc(), follow.id.desc())
        .limit(limit + 1L)
        .fetch();
  }

  private long countFollows(UUID baseId, String nameLike, Direction direction) {
    JPAQuery<Long> query = queryFactory.select(follow.count()).from(follow);

    if (StringUtils.hasText(nameLike)) {
      query.join(user).on(direction.joinCondition());
    }

    return Optional.ofNullable(
        query.where(
            direction.baseCondition(baseId),
            userNameContains(nameLike)
        ).fetchOne()
    ).orElse(0L);
  }

  private BooleanExpression userNameContains(String name) {
    return StringUtils.hasText(name) ? user.name.containsIgnoreCase(name) : null;
  }

  private BooleanExpression cursorCondition(String cursor, UUID idAfter) {
    if (!StringUtils.hasText(cursor)) {
      return null;
    }
    Instant instant = Instant.parse(cursor);
    return follow.createdAt.lt(instant)
        .or(follow.createdAt.eq(instant).and(follow.id.lt(idAfter)));
  }

  private enum Direction {
    FOLLOWINGS {
      @Override
      BooleanExpression baseCondition(UUID id) {
        return follow.followerId.eq(id);
      }

      @Override
      BooleanExpression joinCondition() {
        return user.id.eq(follow.followeeId);
      }

      @Override
      UUID otherSideId(Follow follow) {
        return follow.getFolloweeId();
      }

      @Override
      FollowDto toDto(FollowMapper mapper, Follow follow, UserSummary base, UserSummary other) {
        return mapper.toDto(follow, base, other); // follower = base, followee = other
      }
    },
    FOLLOWERS {
      @Override
      BooleanExpression baseCondition(UUID id) {
        return follow.followeeId.eq(id);
      }

      @Override
      BooleanExpression joinCondition() {
        return user.id.eq(follow.followerId);
      }

      @Override
      UUID otherSideId(Follow follow) {
        return follow.getFollowerId();
      }

      @Override
      FollowDto toDto(FollowMapper mapper, Follow follow, UserSummary base, UserSummary other) {
        return mapper.toDto(follow, other, base); // follower = other, followee = base
      }
    };

    abstract BooleanExpression baseCondition(UUID id);

    abstract BooleanExpression joinCondition();

    abstract UUID otherSideId(Follow follow);

    abstract FollowDto toDto(FollowMapper mapper, Follow follow, UserSummary base,
        UserSummary other);
  }
}