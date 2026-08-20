package com.sprint.mission.otboo.batch.feedreindex.listener;

import com.sprint.mission.otboo.batch.feedreindex.metrics.FeedReindexMetrics;
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
    return null;
  }
}
