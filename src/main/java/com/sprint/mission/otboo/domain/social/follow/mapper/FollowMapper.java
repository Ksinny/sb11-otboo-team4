package com.sprint.mission.otboo.domain.social.follow.mapper;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import org.springframework.stereotype.Component;

@Component
public class FollowMapper {

  public FollowDto toDto(Follow follow, UserSummary follower, UserSummary followee) {
    return new FollowDto(follow.getId(), followee, follower);
  }
}