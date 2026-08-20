package com.sprint.mission.otboo.batch.feedreindex.listener;

import com.sprint.mission.otboo.batch.feedreindex.metrics.FeedReindexMetrics;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FeedReindexStepListener implements StepExecutionListener {

  private final FeedReindexMetrics feedReindexMetrics;

  @Override
  public ExitStatus afterStep(StepExecution stepExecution) {
    if (stepExecution.getStartTime() == null || stepExecution.getEndTime() == null) {
      log.warn("FeedReindex Step 시간 정보 누락 | startTime={}, endTime={}",
          stepExecution.getStartTime(), stepExecution.getEndTime());
      return stepExecution.getExitStatus();
    }

    Duration duration = Duration.between(stepExecution.getStartTime(), stepExecution.getEndTime());
    ExitStatus exitStatus = stepExecution.getExitStatus();

    feedReindexMetrics.countReindexed(stepExecution.getStepName(),
        stepExecution.getWriteCount());

    if (ExitStatus.FAILED.getExitCode().equals(exitStatus.getExitCode())) {
      log.error("FeedReindex Step 실패 | readCount={}, writeCount={}, duration={}, exitStatus={}",
          stepExecution.getReadCount(), stepExecution.getWriteCount(), duration, exitStatus);
    } else {
      log.info("FeedReindex Step 완료 | readCount={}, writeCount={}, duration={}",
          stepExecution.getReadCount(), stepExecution.getWriteCount(), duration);
    }

    return exitStatus;
  }
}
