package com.sprint.mission.otboo.batch.feedreindex.scheduler;

import com.sprint.mission.otboo.batch.feedreindex.service.FeedReindexService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedReindexScheduler {

  private static final String LOCK_NAME = "FeedReindexBatchSchedulerLock";

  private final FeedReindexService feedReindexService;

  // 전체와 증분이 동시에 돌면 읽기 시점 차이로 오래된 문서가 최신을 덮을 수 있어
  // 같은 락으로 상호 배제한다. 증분이 스킵되어도 다음 실행이 lookback으로 덮는다.
  @SchedulerLock(
      name = LOCK_NAME,
      lockAtMostFor = "${batch.feed-reindex.lock-at-most-for:PT30M}",
      lockAtLeastFor = "${batch.feed-reindex.lock-at-least-for:PT1M}")
  @Scheduled(cron = "0 0 5 * * SUN", zone = "Asia/Seoul")
  public void reindexAll() {
    feedReindexService.executeReindexAll();
  }

  // 전체 재색인(일요일 05:00)과 겹치지 않도록 정각이 아닌 30분에 실행한다.
  @SchedulerLock(
      name = LOCK_NAME,
      lockAtMostFor = "${batch.feed-reindex.lock-at-most-for:PT30M}",
      lockAtLeastFor = "${batch.feed-reindex.incremental-lock-at-least-for:PT30S}")
  @Scheduled(cron = "0 30 * * * *", zone = "Asia/Seoul")
  public void reindexIncremental() {
    feedReindexService.executeIncrementalReindex();
  }
}
