package com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.feed.document.FeedDocument;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSearchResult;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSortBy;
import com.sprint.mission.otboo.domain.social.feed.dto.WeatherSnapshot;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.PrecipitationType;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.SkyStatus;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.elasticsearch.test.autoconfigure.DataElasticsearchTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ActiveProfiles;

@DataElasticsearchTest
@ActiveProfiles("test")
@DisplayName("FeedSearchRepository")
class FeedSearchRepositoryTest {

  static final WeatherSnapshot DUMMY_SNAPSHOT = new WeatherSnapshot(
      SkyStatus.CLEAR, PrecipitationType.NONE,
      0.0, 0.0, 28.0, 2.0, 16.0, 31.0);

  @Autowired
  private FeedSearchRepository feedSearchRepository;

  @Autowired
  private ElasticsearchOperations operations;

  private static void setField(Object target, String name, Object value) {
    try {
      var field = target.getClass().getDeclaredField(name);
      field.setAccessible(true);
      field.set(target, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  void tearDown() {
    feedSearchRepository.deleteAll();
    operations.indexOps(FeedDocument.class).refresh();
  }

  // 인덱싱 후 즉시 검색되도록 refresh한다. ES는 기본 1초 주기로 인덱스를 갱신한다.
  private UUID indexFeed(UUID authorId, String content, SkyStatus sky,
      PrecipitationType precipitation, Instant createdAt, long likeCount) {
    UUID feedId = UUID.randomUUID();
    WeatherSnapshot snapshot = new WeatherSnapshot(
        sky, precipitation, 0.0, 0.0, 28.0, 2.0, 16.0, 31.0);
    Feed feed = Feed.create(authorId, UUID.randomUUID(), content, snapshot, List.of());
    setField(feed, "id", feedId);
    setField(feed, "createdAt", createdAt);
    setField(feed, "likeCount", likeCount);

    feedSearchRepository.save(FeedDocument.from(feed));
    operations.indexOps(FeedDocument.class).refresh();
    return feedId;
  }

  private UUID indexFeed(String content, SkyStatus sky, PrecipitationType precipitation,
      Instant createdAt, long likeCount) {
    return indexFeed(UUID.randomUUID(), content, sky, precipitation, createdAt, likeCount);
  }

  private FeedListParams params(String keywordLike) {
    return new FeedListParams(null, null, 10,
        FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
        keywordLike, null, null, null);
  }

  @Nested
  @DisplayName("keywordLike 검색")
  class SearchByKeyword {

    @Test
    @DisplayName("검색어가 포함된 피드만 반환한다")
    void 검색어가_포함된_피드만_반환한다() {
      // given
      UUID matched = indexFeed("따뜻한 니트를 입었어요", SkyStatus.CLEAR,
          PrecipitationType.NONE, Instant.parse("2026-08-01T00:00:00Z"), 0L);
      indexFeed("시원한 반팔", SkyStatus.CLEAR,
          PrecipitationType.NONE, Instant.parse("2026-08-02T00:00:00Z"), 0L);

      // when
      FeedSearchResult result = feedSearchRepository.search(params("니트"));

      // then
      assertThat(result.feedIds()).containsExactly(matched);
      assertThat(result.totalCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("검색어가 없으면 전체를 반환한다")
    void 검색어가_없으면_전체를_반환한다() {
      // given
      indexFeed("따뜻한 니트", SkyStatus.CLEAR,
          PrecipitationType.NONE, Instant.parse("2026-08-01T00:00:00Z"), 0L);
      indexFeed("시원한 반팔", SkyStatus.CLEAR,
          PrecipitationType.NONE, Instant.parse("2026-08-02T00:00:00Z"), 0L);

      // when
      FeedSearchResult result = feedSearchRepository.search(params(null));

      // then
      assertThat(result.feedIds()).hasSize(2);
      assertThat(result.totalCount()).isEqualTo(2L);
    }
  }

  @Nested
  @DisplayName("필터 조회")
  class SearchByFilter {

    @Test
    @DisplayName("skyStatusEqual이 주어지면 해당 하늘 상태의 피드만 반환한다")
    void skyStatusEqual이_주어지면_해당_하늘_상태의_피드만_반환한다() {
      // given
      UUID clear = indexFeed("맑은 날", SkyStatus.CLEAR,
          PrecipitationType.NONE, Instant.parse("2026-08-01T00:00:00Z"), 0L);
      indexFeed("흐린 날", SkyStatus.CLOUDY,
          PrecipitationType.NONE, Instant.parse("2026-08-02T00:00:00Z"), 0L);

      FeedListParams params = new FeedListParams(null, null, 10,
          FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
          null, SkyStatus.CLEAR, null, null);

      // when
      FeedSearchResult result = feedSearchRepository.search(params);

      // then
      assertThat(result.feedIds()).containsExactly(clear);
    }

    @Test
    @DisplayName("precipitationTypeEqual이 주어지면 해당 강수 유형의 피드만 반환한다")
    void precipitationTypeEqual이_주어지면_해당_강수_유형의_피드만_반환한다() {
      // given
      UUID rainy = indexFeed("비 오는 날", SkyStatus.CLOUDY,
          PrecipitationType.RAIN, Instant.parse("2026-08-01T00:00:00Z"), 0L);
      indexFeed("맑은 날", SkyStatus.CLEAR,
          PrecipitationType.NONE, Instant.parse("2026-08-02T00:00:00Z"), 0L);

      FeedListParams params = new FeedListParams(null, null, 10,
          FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
          null, null, PrecipitationType.RAIN, null);

      // when
      FeedSearchResult result = feedSearchRepository.search(params);

      // then
      assertThat(result.feedIds()).containsExactly(rainy);
    }

    @Test
    @DisplayName("authorIdEqual이 주어지면 해당 작성자의 피드만 반환한다")
    void authorIdEqual이_주어지면_해당_작성자의_피드만_반환한다() {
      // given
      UUID targetAuthor = UUID.randomUUID();
      UUID matched = indexFeed(targetAuthor, "대상 작성자 글", SkyStatus.CLEAR,
          PrecipitationType.NONE, Instant.parse("2026-08-01T00:00:00Z"), 0L);
      indexFeed("다른 작성자 글", SkyStatus.CLEAR,
          PrecipitationType.NONE, Instant.parse("2026-08-02T00:00:00Z"), 0L);

      FeedListParams params = new FeedListParams(null, null, 10,
          FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
          null, null, null, targetAuthor);

      // when
      FeedSearchResult result = feedSearchRepository.search(params);

      // then
      assertThat(result.feedIds()).containsExactly(matched);
    }
  }
}
