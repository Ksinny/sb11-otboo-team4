package com.sprint.mission.otboo.domain.social.follow.dto;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import java.util.UUID;

public record FollowDto(
    UUID id,
    UserSummary followee,
    UserSummary follower
) {

}