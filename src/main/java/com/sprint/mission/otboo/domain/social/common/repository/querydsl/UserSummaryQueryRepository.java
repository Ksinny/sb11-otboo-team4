package com.sprint.mission.otboo.domain.social.common.repository.querydsl;

import static com.sprint.mission.otboo.domain.authuser.user.entity.QProfile.profile;
import static com.sprint.mission.otboo.domain.authuser.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sprint.mission.otboo.domain.authuser.user.exception.UserNotFoundException;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserSummaryQueryRepository {

  private final JPAQueryFactory queryFactory;

  public UserSummary findByUserId(UUID userId) {
    UserSummary result = queryFactory
        .select(Projections.constructor(UserSummary.class,
            user.id, user.name, profile.profileImageUrl))
        .from(user)
        .leftJoin(profile).on(profile.id.eq(user.id))
        .where(user.id.eq(userId))
        .fetchOne();

    if (result == null) {
      throw UserNotFoundException.withNone();
    }
    return result;
  }
}