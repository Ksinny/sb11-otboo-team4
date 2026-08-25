package com.sprint.mission.otboo.batch.feedreindex.policy;

import com.sprint.mission.otboo.batch.feedreindex.config.FeedReindexProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Component;

/**
 * 재색인 배치의 skip 판단.
 *
 * <p>예외를 나누는 기준은 {@code FeedIndexEventListener}와 같다.
 * 연결 실패는 그 시점의 모든 인덱싱이 실패 중이라는 뜻이라 skip하지 않고, 매핑 오류 같은 개별 문서 문제만 skip한다.
 */
@Component
@RequiredArgsConstructor
public class FeedReindexSkipPolicy implements SkipPolicy {

  private final FeedReindexProperties properties;

  @Override
  public boolean shouldSkip(Throwable t, long skipCount) {
    if (t instanceof DataAccessResourceFailureException) {
      return false;
    }
    return t instanceof DataAccessException && skipCount < properties.skipLimit();
  }
}
