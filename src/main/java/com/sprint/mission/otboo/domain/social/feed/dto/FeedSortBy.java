package com.sprint.mission.otboo.domain.social.feed.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public enum FeedSortBy {
  @JsonProperty("createdAt")
  CREATED_AT,

  @JsonProperty("likeCount")
  LIKE_COUNT
}