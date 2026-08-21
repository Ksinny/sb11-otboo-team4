package com.sprint.mission.otboo.batch.feedreindex.exception;

public class FeedReindexJobFailedException extends RuntimeException {

  private FeedReindexJobFailedException(Throwable cause) {
    super("피드 재색인 배치 실행 실패", cause);
  }

  public static FeedReindexJobFailedException wrap(Throwable cause) {
    return new FeedReindexJobFailedException(cause);
  }
}
