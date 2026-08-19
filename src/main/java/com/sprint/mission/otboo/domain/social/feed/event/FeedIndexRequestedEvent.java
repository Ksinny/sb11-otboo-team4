package com.sprint.mission.otboo.domain.social.feed.event;

import java.util.UUID;

/**
 * 피드 검색 인덱스 동기화 요청 이벤트.
 *
 * <p><b>{@code feeds} 테이블을 변경하는 모든 경로는 이 이벤트를 발행해야 한다.</b>
 * 누락되면 검색 결과가 실제 데이터와 어긋나며, 예외도 로그도 남지 않는다.
 *
 * <ul>
 *   <li>등록·수정·좋아요 증감 → {@link #upsert(UUID)}
 *   <li>소프트 삭제 → {@link #delete(UUID)}
 * </ul>
 *
 * <p>{@code @TransactionalEventListener(AFTER_COMMIT)}에서 처리되므로 같은 트랜잭션 안에서
 * 발행하면 커밋 이후에 인덱싱된다. 발행을 빠뜨려도 정합성 배치가 주기적으로 보정하지만,
 * 그때까지는 해당 피드가 검색되지 않는다.
 */
public record FeedIndexRequestedEvent(UUID feedId, IndexAction action) {

  public static FeedIndexRequestedEvent upsert(UUID feedId) {
    return new FeedIndexRequestedEvent(feedId, IndexAction.UPSERT);
  }

  public static FeedIndexRequestedEvent delete(UUID feedId) {
    return new FeedIndexRequestedEvent(feedId, IndexAction.DELETE);
  }

  public enum IndexAction {
    UPSERT, DELETE
  }
}
