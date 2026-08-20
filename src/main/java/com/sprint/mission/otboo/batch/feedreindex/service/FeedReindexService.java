package com.sprint.mission.otboo.batch.feedreindex.service;

import com.sprint.mission.otboo.batch.feedreindex.exception.FeedReindexJobFailedException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedReindexService {

  private final JobOperator jobOperator;

  @Qualifier("feedReindexJob")
  private final Job feedReindexJob;

  @Qualifier("feedIncrementalReindexJob")
  private final Job feedIncrementalReindexJob;

  public void execute() {
    run(feedReindexJob, baseParameters().toJobParameters());
  }

  public void executeIncremental(Instant since) {
    run(feedIncrementalReindexJob,
        baseParameters().addLong("since", since.toEpochMilli()).toJobParameters());
  }

  private JobParametersBuilder baseParameters() {
    return new JobParametersBuilder().addLong("time", Instant.now().toEpochMilli());
  }

  private void run(Job job, JobParameters params) {
    try {
      JobExecution execution = jobOperator.start(job, params);
      if (execution.getStatus() != BatchStatus.COMPLETED) {
        throw FeedReindexJobFailedException.wrap(
            new IllegalStateException("Job 상태=" + execution.getStatus()));
      }
    } catch (JobExecutionAlreadyRunningException | JobRestartException
             | JobInstanceAlreadyCompleteException | InvalidJobParametersException e) {
      throw FeedReindexJobFailedException.wrap(e);
    }
  }
}
