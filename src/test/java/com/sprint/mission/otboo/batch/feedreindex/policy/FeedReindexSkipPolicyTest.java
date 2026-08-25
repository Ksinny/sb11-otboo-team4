package com.sprint.mission.otboo.batch.feedreindex.policy;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.batch.feedreindex.config.FeedReindexProperties;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.elasticsearch.BulkFailureException;
import org.springframework.data.elasticsearch.BulkFailureException.FailureDetails;
import org.springframework.data.elasticsearch.UncategorizedElasticsearchException;
import org.springframework.data.elasticsearch.VersionConflictException;

@DisplayName("FeedReindexSkipPolicy")
class FeedReindexSkipPolicyTest {

  private final FeedReindexSkipPolicy policy =
      new FeedReindexSkipPolicy(new FeedReindexProperties(500, 10, Duration.ofHours(2)));

  @Nested
  @DisplayName("skip 판단")
  class ShouldSkip {

    @Test
    @DisplayName("연결 실패는 skip하지 않는다")
    void 연결_실패는_skip하지_않는다() {
      // given
      Throwable t = new DataAccessResourceFailureException("Connection refused");

      // when
      boolean result = policy.shouldSkip(t, 0);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("개별 문서 오류는 skip한다")
    void 개별_문서_오류는_skip한다() {
      // given
      Throwable t = new UncategorizedElasticsearchException("mapper_parsing_exception");

      // when
      boolean result = policy.shouldSkip(t, 0);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("skipLimit을 넘으면 skip하지 않는다")
    void skipLimit을_넘으면_skip하지_않는다() {
      // given
      Throwable t = new UncategorizedElasticsearchException("mapper_parsing_exception");

      // when
      boolean result = policy.shouldSkip(t, 10);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("DataAccessException이 아닌 예외는 skip하지 않는다")
    void DataAccessException이_아닌_예외는_skip하지_않는다() {
      // given
      Throwable t = new IllegalStateException("예상 밖");

      // when
      boolean result = policy.shouldSkip(t, 0);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("버전 충돌은 개수 제한 없이 건너뛴다")
    void 버전_충돌은_개수_제한_없이_건너뛴다() {
      // given
      Throwable t = new VersionConflictException("version conflict");

      // when & then
      assertThat(policy.shouldSkip(t, 100)).isTrue();
    }

    @Test
    @DisplayName("bulk 버전 충돌도 개수 제한 없이 건너뛴다")
    void bulk_버전_충돌도_개수_제한_없이_건너뛴다() {
      // given
      Throwable t = new BulkFailureException("Bulk operation has failures",
          Map.of("feed-1", new FailureDetails(409, "version conflict")));

      // when & then
      assertThat(policy.shouldSkip(t, 100)).isTrue();
    }

    @Test
    @DisplayName("bulk 실패에 409가 아닌 것이 섞이면 건너뛰지 않는다")
    void bulk_실패에_409가_아닌_것이_섞이면_건너뛰지_않는다() {
      // given
      Throwable t = new BulkFailureException("Bulk operation has failures",
          Map.of("feed-1", new FailureDetails(409, "version conflict"),
              "feed-2", new FailureDetails(400, "mapper_parsing_exception")));

      // when & then
      assertThat(policy.shouldSkip(t, 100)).isFalse();
    }
  }
}
