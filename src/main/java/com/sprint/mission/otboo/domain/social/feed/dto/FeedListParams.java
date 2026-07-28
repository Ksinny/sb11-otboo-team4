package com.sprint.mission.otboo.domain.social.feed.dto;

import com.sprint.mission.otboo.global.dto.SortDirection;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FeedListParams(
    String cursor,
    UUID idAfter,
    @NotNull @Min(1) @Max(100) Integer limit,
    @NotNull FeedSortBy sortBy,
    @NotNull SortDirection sortDirection,
    String keywordLike,
    // TODO: 날씨 도메인 추가 후 필터 필드 복원
    //   skyStatusEqual, precipitationTypeEqual — 스냅샷 컬럼 채움 후 유효
    UUID authorIdEqual
) {

  @AssertTrue(message = "cursor, idAfter는 함께 전달되어야 합니다")
  public boolean isCursorAndIdAfterConsistent() {
    return (cursor == null && idAfter == null) || (cursor != null && idAfter != null);
  }

  @AssertTrue(message = "likeCount 기준 커서는 숫자여야 합니다")
  public boolean isCursorFormatValidForSortBy() {
    if (cursor == null || sortBy != FeedSortBy.LIKE_COUNT) {
      return true;
    }
    try {
      Long.parseLong(cursor);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}