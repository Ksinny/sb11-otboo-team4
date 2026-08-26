package com.sprint.mission.otboo.batch.feedmigration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedIndexMigrationService {

  private final JobOperator jobOperator;
  private final ElasticsearchOperations elasticsearchOperations;

  @Qualifier("feedIndexMigrationJob")
  private final Job feedIndexMigrationJob;

  public void migrate() {
  }
}
