package com.sprint.mission.otboo.batch.feedreindex.support;

import org.springframework.data.elasticsearch.BulkFailureException;
import org.springframework.data.elasticsearch.VersionConflictException;

/**
 * ES 버전 충돌 판정.
 *
 * <p>단건 {@code save}는 {@link VersionConflictException}을, bulk({@code saveAll})는 409를
 * {@link BulkFailureException}으로 감싸 던진다. skip 판단과 로그 분기가 같은 기준을 쓰도록 모아둔다.
 */
public final class VersionConflicts {

  private static final int STATUS_CONFLICT = 409;

  private VersionConflicts() {
  }

  public static boolean isVersionConflict(Throwable t) {
    if (t instanceof VersionConflictException) {
      return true;
    }
    // 409가 아닌 실패가 섞여 있으면 진짜 문제이므로 충돌로 보지 않는다.
    return t instanceof BulkFailureException e
        && !e.getFailedDocuments().isEmpty()
        && e.getFailedDocuments().values().stream()
        .allMatch(detail -> detail.status() == STATUS_CONFLICT);
  }
}
