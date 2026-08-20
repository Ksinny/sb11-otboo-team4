package com.sprint.mission.otboo.batch.feedreindex.service;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
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
  }

  public void executeIncremental(Instant since) {
  }
}
