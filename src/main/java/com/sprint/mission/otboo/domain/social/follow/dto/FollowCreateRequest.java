package com.sprint.mission.otboo.domain.social.follow.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FollowCreateRequest(
    @NotNull UUID followeeId,
    @NotNull UUID followerId
) {

}