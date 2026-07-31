package com.sprint.mission.otboo.domain.social.follow.mapper;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowSummaryDto;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FollowMapper {

  public FollowDto toDto(Follow follow, UserSummary follower, UserSummary followee) {
    return new FollowDto(follow.getId(), followee, follower);
  }

  public FollowSummaryDto toSummaryDto(UUID followeeId, long followerCount, long followingCount,
      boolean followedByMe, UUID followedByMeId, boolean followingMe
  ) {
    return new FollowSummaryDto(
        followeeId, followerCount, followingCount, followedByMe, followedByMeId, followingMe);
  }
}