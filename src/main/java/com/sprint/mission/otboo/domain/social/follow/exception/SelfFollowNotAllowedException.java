package com.sprint.mission.otboo.domain.social.follow.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;

public class SelfFollowNotAllowedException extends FollowException {

  private SelfFollowNotAllowedException() {
    super(HttpStatus.BAD_REQUEST, "자기 자신을 팔로우할 수 없습니다.", Map.of());
  }

  public static SelfFollowNotAllowedException withNone() {
    return new SelfFollowNotAllowedException();
  }
}