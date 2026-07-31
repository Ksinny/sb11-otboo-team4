package com.sprint.mission.otboo.domain.social.follow.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import org.springframework.util.StringUtils;

public record FollowingListParams(
    @NotNull UUID followerId,
    String cursor,
    UUID idAfter,
    @NotNull @Min(1) @Max(100) Integer limit,
    String nameLike
) {

  @AssertTrue(message = "cursor, idAfter는 함께 전달되어야 합니다")
  public boolean isCursorAndIdAfterConsistent() {
    return StringUtils.hasText(cursor) == (idAfter != null);
  }

  @AssertTrue(message = "cursor는 Instant 형식이어야 합니다")
  public boolean isCursorFormatValid() {
    if (!StringUtils.hasText(cursor)) {
      return true;
    }
    try {
      Instant.parse(cursor);
      return true;
    } catch (DateTimeParseException e) {
      return false;
    }
  }
}