package com.sprint.mission.otboo.batch.feedmigration.exception;

public class FeedIndexNameException extends RuntimeException {

  private FeedIndexNameException(String indexName, String expectedPattern) {
    super("인덱스 이름이 " + expectedPattern + " 규칙에 맞지 않습니다: " + indexName);
  }

  public static FeedIndexNameException of(String indexName, String expectedPattern) {
    return new FeedIndexNameException(indexName, expectedPattern);
  }
}
