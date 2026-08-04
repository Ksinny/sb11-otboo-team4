package com.sprint.mission.otboo.domain.social.feed.exception;

import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;

public class ClothesOwnershipException extends FeedException {

  private static final String MESSAGE = "본인 소유의 의상만 피드에 등록할 수 있습니다.";

  private ClothesOwnershipException(Map<String, Object> details) {
    super(HttpStatus.FORBIDDEN, MESSAGE, details);
  }

  public static ClothesOwnershipException of(UUID authorId) {
    return new ClothesOwnershipException(Map.of("authorId", authorId));
  }
}