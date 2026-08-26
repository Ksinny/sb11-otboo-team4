package com.sprint.mission.otboo.batch.feedmigration.service;

import com.sprint.mission.otboo.batch.feedmigration.exception.FeedIndexMigrationFailedException;
import com.sprint.mission.otboo.domain.social.feed.document.FeedDocument;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

/**
 * 매핑 변경 시 새 인덱스로 무중단 전환한다.
 *
 * <p>재색인 소스는 DB다. {@code copy_to}가 색인 시점에만 동작해 {@code _reindex}로 인덱스를
 * 복사하면 {@code searchText}가 채워지지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedIndexMigrationService {

  private final JobOperator jobOperator;
  private final ElasticsearchOperations elasticsearchOperations;

  @Qualifier("feedIndexMigrationJob")
  private final Job feedIndexMigrationJob;

  public void migrate() {
    String currentIndex = currentIndexBehindAlias();
    String newIndex = FeedIndexNames.nextVersionOf(currentIndex);
    log.info("피드 인덱스 마이그레이션 시작: from={}, to={}", currentIndex, newIndex);

    createIndex(newIndex);
    reindexInto(newIndex);
  }

  private String currentIndexBehindAlias() {
    return aliasOps().getAliases(FeedDocument.INDEX_NAME).keySet().iterator().next();
  }

  private void createIndex(String newIndex) {
    IndexOperations entityOps = elasticsearchOperations.indexOps(FeedDocument.class);
    indexOps(newIndex).create(entityOps.createSettings(), entityOps.createMapping());
    log.info("새 피드 인덱스 생성 완료: index={}", newIndex);
  }

  private void reindexInto(String newIndex) {
    JobParameters parameters = new JobParametersBuilder()
        .addLong("time", Instant.now().toEpochMilli())
        .addString("targetIndex", newIndex)
        .toJobParameters();
    try {
      jobOperator.start(feedIndexMigrationJob, parameters);
    } catch (JobExecutionAlreadyRunningException | JobRestartException
             | JobInstanceAlreadyCompleteException | InvalidJobParametersException e) {
      throw FeedIndexMigrationFailedException.wrap(e);
    }
  }

  private IndexOperations aliasOps() {
    return indexOps(FeedDocument.INDEX_NAME);
  }

  private IndexOperations indexOps(String indexName) {
    return elasticsearchOperations.indexOps(IndexCoordinates.of(indexName));
  }
}
