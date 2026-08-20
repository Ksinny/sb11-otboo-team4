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
import java.util.List;
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
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedReindexJobListener")
class FeedReindexJobListenerTest {

  @InjectMocks
  private FeedReindexJobListener listener;

  @Mock
  private FeedReindexMetrics feedReindexMetrics;

  @Mock
  private JobExecution jobExecution;

  private Logger logger;
  private ListAppender<ILoggingEvent> appender;

  @BeforeEach
  void setUp() {
    logger = (Logger) LoggerFactory.getLogger(FeedReindexJobListener.class);
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
  @DisplayName("Job 시작")
  class BeforeJob {

    @Test
    @DisplayName("Job 시작 정보를 로그로 남긴다")
    void Job_시작_정보를_로그로_남긴다() {
      // given
      given(jobExecution.getId()).willReturn(1L);
      given(jobExecution.getJobParameters()).willReturn(new JobParameters());

      // when
      listener.beforeJob(jobExecution);

      // then
      assertThat(appender.list).anySatisfy(
          event -> assertThat(event.getLevel()).isEqualTo(Level.INFO));
    }
  }

  @Nested
  @DisplayName("Job 종료")
  class AfterJob {

    @Test
    @DisplayName("성공하면 성공 로그와 메트릭을 남긴다")
    void 성공하면_성공_로그와_메트릭을_남긴다() {
      // given
      given(jobExecution.getStatus()).willReturn(BatchStatus.COMPLETED);
      given(jobExecution.getAllFailureExceptions()).willReturn(List.of());

      // when
      listener.afterJob(jobExecution);

      // then
      verify(feedReindexMetrics).countCompleted();
      assertThat(appender.list).anySatisfy(event -> {
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event.getFormattedMessage()).contains("성공");
      });
    }

    @Test
    @DisplayName("실패하면 실패 로그와 메트릭을 남긴다")
    void 실패하면_실패_로그와_메트릭을_남긴다() {
      // given
      given(jobExecution.getStatus()).willReturn(BatchStatus.FAILED);
      given(jobExecution.getExitStatus()).willReturn(ExitStatus.FAILED);
      given(jobExecution.getAllFailureExceptions()).willReturn(List.of());

      // when
      listener.afterJob(jobExecution);

      // then
      verify(feedReindexMetrics).countFailed();
      assertThat(appender.list).anySatisfy(event -> {
        assertThat(event.getLevel()).isEqualTo(Level.ERROR);
        assertThat(event.getFormattedMessage()).contains("실패");
      });
    }

    @Test
    @DisplayName("시작과 종료 시각이 있으면 소요시간을 기록한다")
    void 시작과_종료_시각이_있으면_소요시간을_기록한다() {
      // given
      given(jobExecution.getStatus()).willReturn(BatchStatus.COMPLETED);
      given(jobExecution.getStartTime()).willReturn(LocalDateTime.of(2026, 8, 20, 5, 0, 0));
      given(jobExecution.getEndTime()).willReturn(LocalDateTime.of(2026, 8, 20, 5, 0, 5));
      given(jobExecution.getAllFailureExceptions()).willReturn(List.of());

      // when
      listener.afterJob(jobExecution);

      // then
      verify(feedReindexMetrics).recordJobDuration(java.time.Duration.ofSeconds(5));
    }
  }
}
