package com.sprint.mission.otboo.batch.feedreindex.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.sprint.mission.otboo.batch.feedreindex.metrics.FeedReindexMetrics;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.step.StepExecution;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedReindexStepListener")
class FeedReindexStepListenerTest {

  @InjectMocks
  private FeedReindexStepListener listener;

  @Mock
  private FeedReindexMetrics feedReindexMetrics;

  @Mock
  private StepExecution stepExecution;

  private Logger logger;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUp() {
    logger = (Logger) LoggerFactory.getLogger(FeedReindexStepListener.class);
    appender = new ListAppender<>();
    appender.start();
    logger.addAppender(appender);
  }

  @AfterEach
  void tearDown() {
    logger.detachAppender(appender);
    appender.stop();
  }

  @Nested
  @DisplayName("Step 종료")
  class AfterStep {

    @Test
    @DisplayName("시작과 종료 시각이 없으면 경고 로그만 남기고 ExitStatus를 그대로 반환한다")
    void 시작과_종료_시각이_없으면_경고_로그만_남기고_ExitStatus를_그대로_반환한다() {
      // given
      given(stepExecution.getStartTime()).willReturn(null);
      given(stepExecution.getEndTime()).willReturn(null);
      given(stepExecution.getExitStatus()).willReturn(ExitStatus.COMPLETED);

      // when
      ExitStatus result = listener.afterStep(stepExecution);

      // then
      assertThat(result).isEqualTo(ExitStatus.COMPLETED);
      assertThat(appender.list).anySatisfy(
          event -> assertThat(event.getLevel()).isEqualTo(Level.WARN));
    }

    @Test
    @DisplayName("정상 종료하면 처리 건수와 소요시간을 로그와 메트릭으로 남긴다")
    void 정상_종료하면_처리_건수와_소요시간을_로그와_메트릭으로_남긴다() {
      // given
      given(stepExecution.getStartTime()).willReturn(LocalDateTime.of(2026, 8, 20, 5, 0, 0));
      given(stepExecution.getEndTime()).willReturn(LocalDateTime.of(2026, 8, 20, 5, 0, 5));
      given(stepExecution.getStepName()).willReturn("feedReindexStep");
      given(stepExecution.getReadCount()).willReturn(10L);
      given(stepExecution.getWriteCount()).willReturn(10L);
      given(stepExecution.getExitStatus()).willReturn(ExitStatus.COMPLETED);

      // when
      ExitStatus result = listener.afterStep(stepExecution);

      // then
      assertThat(result).isEqualTo(ExitStatus.COMPLETED);
      verify(feedReindexMetrics).countReindexed("feedReindexStep", 10L);
      assertThat(appender.list).anySatisfy(event -> {
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).contains("완료");
      });
    }

    @Test
    @DisplayName("실패로 종료하면 오류 로그를 남긴다")
    void 실패로_종료하면_오류_로그를_남긴다() {
      // given
      given(stepExecution.getStartTime()).willReturn(LocalDateTime.of(2026, 8, 20, 5, 0, 0));
      given(stepExecution.getEndTime()).willReturn(LocalDateTime.of(2026, 8, 20, 5, 0, 5));
      given(stepExecution.getStepName()).willReturn("feedReindexStep");
      given(stepExecution.getReadCount()).willReturn(5L);
      given(stepExecution.getWriteCount()).willReturn(3L);
      given(stepExecution.getExitStatus()).willReturn(ExitStatus.FAILED);

      // when
      ExitStatus result = listener.afterStep(stepExecution);

      // then
      assertThat(result).isEqualTo(ExitStatus.FAILED);
      assertThat(appender.list).anySatisfy(event -> {
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getFormattedMessage()).contains("실패");
      });
    }
  }
}
