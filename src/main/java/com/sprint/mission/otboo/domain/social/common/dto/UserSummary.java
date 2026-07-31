package com.sprint.mission.otboo.domain.social.common.dto;

import java.util.UUID;

public record UserSummary(
    UUID userId,
    String name,
    String profileImageUrl
) {

}