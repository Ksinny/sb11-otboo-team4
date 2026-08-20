package com.sprint.mission.otboo.batch.feedreindex.listener;

import com.sprint.mission.otboo.batch.feedreindex.metrics.FeedReindexMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FeedReindexJobListener implements JobExecutionListener {

  private final FeedReindexMetrics feedReindexMetrics;

  @Override
  public void beforeJob(JobExecution jobExecution) {
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
  }
}
