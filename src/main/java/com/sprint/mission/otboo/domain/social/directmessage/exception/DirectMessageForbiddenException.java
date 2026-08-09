package com.sprint.mission.otboo.domain.social.directmessage.exception;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class DirectMessageForbiddenException extends DirectMessageException {

  private static final String MESSAGE = "본인만 메시지를 보낼 수 있습니다.";

  private DirectMessageForbiddenException(Map<String, Object> details) {
    super(HttpStatus.FORBIDDEN, MESSAGE, details);
  }

  public static DirectMessageForbiddenException senderMismatch(UUID current, UUID requested) {
    return new DirectMessageForbiddenException(
        Map.of("currentUserId", current, "requestedSenderId", requested));
  }
}