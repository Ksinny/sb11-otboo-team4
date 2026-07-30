package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class FollowForbiddenException extends FollowException {

  private FollowForbiddenException() {
    super(HttpStatus.FORBIDDEN, "본인만 수행할 수 있습니다.", Map.of());
  }

  public static FollowForbiddenException followerMismatch() {
    return new FollowForbiddenException();
  }
}