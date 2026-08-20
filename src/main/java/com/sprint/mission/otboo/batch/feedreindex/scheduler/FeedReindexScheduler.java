package com.sprint.mission.otboo.batch.feedreindex.scheduler;

import com.sprint.mission.otboo.batch.feedreindex.config.FeedReindexProperties;
import com.sprint.mission.otboo.batch.feedreindex.service.FeedReindexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedReindexScheduler {

  private final FeedReindexService feedReindexService;
  private final FeedReindexProperties feedReindexProperties;

  @SchedulerLock(
      name = "FeedReindexBatchSchedulerLock",
      lockAtMostFor = "${batch.feed-reindex.lock-at-most-for:PT30M}",
      lockAtLeastFor = "${batch.feed-reindex.lock-at-least-for:PT1M}")
  @Scheduled(cron = "0 0 5 * * SUN", zone = "Asia/Seoul")
  public void reindexAll() {
  }

  // 전체 재색인(일요일 05:00)과 겹치지 않도록 정각이 아닌 30분에 실행한다.
  @SchedulerLock(
      name = "FeedIncrementalReindexBatchSchedulerLock",
      lockAtMostFor = "${batch.feed-reindex.incremental-lock-at-most-for:PT10M}",
      lockAtLeastFor = "${batch.feed-reindex.incremental-lock-at-least-for:PT30S}")
  @Scheduled(cron = "0 30 * * * *", zone = "Asia/Seoul")
  public void reindexIncremental() {
  }
}
