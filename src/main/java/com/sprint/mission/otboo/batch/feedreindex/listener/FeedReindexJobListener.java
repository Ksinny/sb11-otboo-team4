package com.sprint.mission.otboo.batch.feedreindex.listener;

import com.sprint.mission.otboo.batch.feedreindex.metrics.FeedReindexMetrics;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
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
    log.info("FeedReindex Job 시작 | jobId={}, params={}", jobExecution.getId(),
        jobExecution.getJobParameters());
  }

  @Override
  public void afterJob(JobExecution jobExecution) {
    if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
      log.info("FeedReindex Job 성공 | jobId={}", jobExecution.getId());
      feedReindexMetrics.countCompleted();
    } else if (jobExecution.getStatus() == BatchStatus.FAILED) {
      log.error("FeedReindex Job 실패 | jobId={}, exitStatus={}", jobExecution.getId(),
          jobExecution.getExitStatus());
      feedReindexMetrics.countFailed();
    }

    if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
      Duration duration = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime());
      log.info("FeedReindex Job duration={}", duration);
      feedReindexMetrics.recordJobDuration(duration);
    }

    if (!jobExecution.getAllFailureExceptions().isEmpty()) {
      jobExecution.getAllFailureExceptions()
          .forEach(e -> log.error("FeedReindex Job 실패 원인", e));
    }
  }
}
