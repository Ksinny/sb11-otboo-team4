package com.sprint.mission.otboo.domain.social.follow.exception;

import com.sprint.mission.otboo.global.exception.OtbooException;
import java.util.Map;
import org.springframework.http.HttpStatus;

public abstract class FollowException extends OtbooException {

  protected FollowException(HttpStatus status, String message, Map<String, Object> details) {
    super(status, message, details);
  }
}