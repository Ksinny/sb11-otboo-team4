package com.sprint.mission.otboo.batch.feedreindex.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.sprint.mission.otboo.batch.feedreindex.config.FeedReindexProperties;
import com.sprint.mission.otboo.batch.feedreindex.service.FeedReindexService;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedReindexScheduler")
class FeedReindexSchedulerTest {

  private FeedReindexScheduler scheduler;

  @Mock
  private FeedReindexService feedReindexService;

  @BeforeEach
  void setUp() {
    scheduler = new FeedReindexScheduler(feedReindexService,
        new FeedReindexProperties(500, Duration.ofHours(2)));
  }

  @Nested
  @DisplayName("전체 재색인")
  class ReindexAll {

    @Test
    @DisplayName("전체 재색인만 호출하고 다른 로직은 없다")
    void 전체_재색인만_호출하고_다른_로직은_없다() {
      // when
      scheduler.reindexAll();

      // then
      verify(feedReindexService).execute();
      verifyNoMoreInteractions(feedReindexService);
    }
  }

  @Nested
  @DisplayName("증분 재색인")
  class ReindexIncremental {

    @Test
    @DisplayName("설정된 lookback 만큼 이전 시각으로 증분 재색인을 호출한다")
    void 설정된_lookback_만큼_이전_시각으로_증분_재색인을_호출한다() {
      // when
      scheduler.reindexIncremental();

      // then
      verify(feedReindexService).executeIncremental(any(Instant.class));
      verifyNoMoreInteractions(feedReindexService);
    }
  }
}
