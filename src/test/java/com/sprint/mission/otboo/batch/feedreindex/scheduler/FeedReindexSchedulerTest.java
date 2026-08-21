package com.sprint.mission.otboo.batch.feedreindex.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.sprint.mission.otboo.batch.feedreindex.service.FeedReindexService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedReindexScheduler")
class FeedReindexSchedulerTest {

  @InjectMocks
  private FeedReindexScheduler scheduler;

  @Mock
  private FeedReindexService feedReindexService;

  @Nested
  @DisplayName("전체 재색인")
  class ReindexAll {

    @Test
    @DisplayName("전체 재색인만 호출하고 다른 로직은 없다")
    void 전체_재색인만_호출하고_다른_로직은_없다() {
      // when
      scheduler.reindexAll();

      // then
      verify(feedReindexService).executeReindexAll();
      verifyNoMoreInteractions(feedReindexService);
    }
  }

  @Nested
  @DisplayName("증분 재색인")
  class ReindexIncremental {

    @Test
    @DisplayName("증분 재색인만 호출하고 다른 로직은 없다")
    void 증분_재색인만_호출하고_다른_로직은_없다() {
      // when
      scheduler.reindexIncremental();

      // then
      verify(feedReindexService).executeIncrementalReindex();
      verifyNoMoreInteractions(feedReindexService);
    }
  }
}
