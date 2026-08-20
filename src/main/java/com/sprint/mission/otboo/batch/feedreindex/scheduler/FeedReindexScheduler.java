package com.sprint.mission.otboo.batch.feedreindex.scheduler;

import com.sprint.mission.otboo.batch.feedreindex.config.FeedReindexProperties;
import com.sprint.mission.otboo.batch.feedreindex.service.FeedReindexService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedReindexScheduler {

  private static final String LOCK_NAME = "FeedReindexBatchSchedulerLock";

  private final FeedReindexService feedReindexService;
  private final FeedReindexProperties feedReindexProperties;

  // 전체와 증분이 동시에 돌면 읽기 시점 차이로 오래된 문서가 최신을 덮을 수 있어
  // 같은 락으로 상호 배제한다. 증분이 스킵되어도 다음 실행이 lookback으로 덮는다.
  @SchedulerLock(
      name = LOCK_NAME,
      lockAtMostFor = "${batch.feed-reindex.lock-at-most-for:PT30M}",
      lockAtLeastFor = "${batch.feed-reindex.lock-at-least-for:PT1M}")
  @Scheduled(cron = "0 0 5 * * SUN", zone = "Asia/Seoul")
  public void reindexAll() {
    log.info("피드 전체 재색인 배치 시작");
    feedReindexService.execute();
    log.info("피드 전체 재색인 배치 완료");
  }

  // 전체 재색인(일요일 05:00)과 겹치지 않도록 정각이 아닌 30분에 실행한다.
  @SchedulerLock(
      name = LOCK_NAME,
      lockAtMostFor = "${batch.feed-reindex.lock-at-most-for:PT30M}",
      lockAtLeastFor = "${batch.feed-reindex.incremental-lock-at-least-for:PT30S}")
  @Scheduled(cron = "0 30 * * * *", zone = "Asia/Seoul")
  public void reindexIncremental() {
    Instant since = Instant.now().minus(feedReindexProperties.incrementalLookback());
    log.info("피드 증분 재색인 배치 시작: since={}", since);
    feedReindexService.executeIncremental(since);
    log.info("피드 증분 재색인 배치 완료");
  }
}
