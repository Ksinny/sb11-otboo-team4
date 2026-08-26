package com.sprint.mission.otboo.batch.feedmigration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.sprint.mission.otboo.domain.social.feed.document.FeedDocument;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.index.Settings;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedIndexMigrationService")
class FeedIndexMigrationServiceTest {

  private static final IndexCoordinates ALIAS = IndexCoordinates.of(FeedDocument.INDEX_NAME);
  private static final IndexCoordinates CURRENT_INDEX = IndexCoordinates.of("feeds_v1");
  private static final IndexCoordinates NEW_INDEX = IndexCoordinates.of("feeds_v2");

  @Mock
  private JobOperator jobOperator;

  @Mock
  private ElasticsearchOperations elasticsearchOperations;

  @Mock
  private Job feedIndexMigrationJob;

  @Mock
  private JobExecution jobExecution;

  @Mock
  private IndexOperations aliasOperations;

  @Mock
  private IndexOperations entityOperations;

  @Mock
  private IndexOperations newIndexOperations;

  private FeedIndexMigrationService feedIndexMigrationService;

  @BeforeEach
  void setUp() {
    feedIndexMigrationService = new FeedIndexMigrationService(
        jobOperator, elasticsearchOperations, feedIndexMigrationJob);
  }

  private void givenAliasPointsToCurrentIndex() {
    given(elasticsearchOperations.indexOps(eq(ALIAS))).willReturn(aliasOperations);
    given(aliasOperations.getAliases(FeedDocument.INDEX_NAME))
        .willReturn(Map.of(CURRENT_INDEX.getIndexName(), Set.of()));
  }

  private void givenNewIndexCreated() {
    given(elasticsearchOperations.indexOps(FeedDocument.class)).willReturn(entityOperations);
    given(entityOperations.createSettings()).willReturn(new Settings());
    given(entityOperations.createMapping()).willReturn(Document.create());
    given(elasticsearchOperations.indexOps(eq(NEW_INDEX))).willReturn(newIndexOperations);
  }

  private void givenJobCompleted() throws Exception {
    given(jobOperator.start(any(Job.class), any(JobParameters.class))).willReturn(jobExecution);
    given(jobExecution.getStatus()).willReturn(BatchStatus.COMPLETED);
  }

  @Nested
  @DisplayName("인덱스 마이그레이션")
  class Migrate {

    @Test
    @DisplayName("alias가 가리키는 인덱스의 다음 버전으로 새 인덱스를 만든다")
    void alias가_가리키는_인덱스의_다음_버전으로_새_인덱스를_만든다() {
      // given
      givenAliasPointsToCurrentIndex();
      givenNewIndexCreated();

      // when
      feedIndexMigrationService.migrate();

      // then
      verify(elasticsearchOperations).indexOps(eq(NEW_INDEX));
      verify(newIndexOperations).create(any(Settings.class), any(Document.class));
    }

    @Test
    @DisplayName("새 인덱스를 대상으로 재색인 Job을 실행한다")
    void 새_인덱스를_대상으로_재색인_Job을_실행한다() throws Exception {
      // given
      givenAliasPointsToCurrentIndex();
      givenNewIndexCreated();

      // when
      feedIndexMigrationService.migrate();

      // then
      ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
      verify(jobOperator).start(eq(feedIndexMigrationJob), captor.capture());
      assertThat(captor.getValue().getString("targetIndex"))
          .isEqualTo(NEW_INDEX.getIndexName());
    }
  }
}
