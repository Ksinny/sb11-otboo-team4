package com.sprint.mission.otboo.domain.social.feed.dto;

import com.sprint.mission.otboo.domain.social.follow.dto.CursorListParams;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record FeedCommentParams(
    @NotNull UUID feedId,
    String cursor,
    UUID idAfter,
    @NotNull @Min(1) @Max(100) Integer limit
) implements CursorListParams {

}