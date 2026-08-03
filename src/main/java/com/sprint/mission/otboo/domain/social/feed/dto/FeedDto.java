package com.sprint.mission.otboo.domain.social.feed.dto;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import java.time.Instant;
import java.util.UUID;

public record FeedDto(
    UUID id,
    Instant createdAt,
    Instant updatedAt,
    UserSummary author,
    // TODO: weather(WeatherSummaryDto), ootds(List<OotdDto>)
    String content,
    long likeCount,
    int commentCount,
    boolean likedByMe
) {

}