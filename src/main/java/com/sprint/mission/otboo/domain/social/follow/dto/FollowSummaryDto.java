package com.sprint.mission.otboo.domain.social.follow.dto;

import java.util.UUID;

public record FollowSummaryDto(
    UUID followeeId,
    long followerCount,
    long followingCount,
    boolean followedByMe,
    UUID followedByMeId,     // 내가 팔로우 중이 아니면 null
    boolean followingMe
) {

}