package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class SelfFollowNotAllowedException extends FollowException {

  private SelfFollowNotAllowedException(Map<String, Object> details) {
    super(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다.", details);
  }

  public static SelfFollowNotAllowedException of(UUID userId) {
    return new SelfFollowNotAllowedException(Map.of("userId", userId));
  }
}