package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class FollowForbiddenException extends FollowException {

  private FollowForbiddenException(Map<String, Object> details) {
    super(HttpStatus.FORBIDDEN, "본인만 수행할 수 있습니다.", details);
  }

  public static FollowForbiddenException followerMismatch(UUID currentUserId,
      UUID requestedFollowerId) {
    return new FollowForbiddenException(
        Map.of("currentUserId", currentUserId, "requestedFollowerId", requestedFollowerId));
  }
}