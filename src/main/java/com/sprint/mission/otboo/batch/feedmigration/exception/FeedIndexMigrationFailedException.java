package com.sprint.mission.otboo.batch.feedmigration.exception;

public class FeedIndexMigrationFailedException extends RuntimeException {

  private FeedIndexMigrationFailedException(Throwable cause) {
    super("피드 인덱스 마이그레이션 실패", cause);
  }

  public static FeedIndexMigrationFailedException wrap(Throwable cause) {
    return new FeedIndexMigrationFailedException(cause);
  }
}
