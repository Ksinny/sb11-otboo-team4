package com.sprint.mission.otboo.domain.social.feed.mapper;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import org.springframework.stereotype.Component;

@Component
public class FeedMapper {

  public FeedDto toDto(Feed feed, UserSummary author, boolean likedByMe) {
    return new FeedDto(
        feed.getId(),
        feed.getCreatedAt(),
        feed.getUpdatedAt(),
        author,
        feed.getContent(),
        feed.getLikeCount(),
        feed.getCommentCount(),
        likedByMe
    );
  }
}