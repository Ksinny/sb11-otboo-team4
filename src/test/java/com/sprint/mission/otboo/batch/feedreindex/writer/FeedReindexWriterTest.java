package com.sprint.mission.otboo.batch.feedreindex.writer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.sprint.mission.otboo.domain.social.feed.document.FeedDocument;
import com.sprint.mission.otboo.domain.social.feed.dto.WeatherSnapshot;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.FeedSearchRepository;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.PrecipitationType;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.SkyStatus;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedReindexWriter")
class FeedReindexWriterTest {

  static final WeatherSnapshot DUMMY_SNAPSHOT = new WeatherSnapshot(
      SkyStatus.CLEAR, PrecipitationType.NONE,
      0.0, 0.0, 28.0, 2.0, 16.0, 31.0);

  @InjectMocks
  private FeedReindexWriter writer;

  @Mock
  private FeedSearchRepository feedSearchRepository;

  @Mock
  private EntityManager entityManager;

  private static Feed feedWith(UUID id, String content) {
    Feed feed = Feed.create(UUID.randomUUID(), UUID.randomUUID(), content,
        DUMMY_SNAPSHOT, List.of());
    setField(feed, "id", id);
    setField(feed, "createdAt", Instant.parse("2026-08-20T01:00:00Z"));
    return feed;
  }

  private static void setField(Feed feed, String name, Object value) {
    try {
      var field = Feed.class.getDeclaredField(name);
      field.setAccessible(true);
      field.set(feed, value);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Nested
  @DisplayName("인덱스 저장")
  class Write {

    @Test
    @DisplayName("청크를 FeedDocument로 변환해 한 번에 저장한다")
    void 청크를_FeedDocument로_변환해_한_번에_저장한다() {
      // given
      UUID id1 = UUID.randomUUID();
      UUID id2 = UUID.randomUUID();
      Chunk<Feed> chunk = new Chunk<>(
          List.of(feedWith(id1, "피드1"), feedWith(id2, "피드2")));

      // when
      writer.write(chunk);

      // then
      ArgumentCaptor<List<FeedDocument>> captor = ArgumentCaptor.forClass(List.class);
      verify(feedSearchRepository).saveAll(captor.capture());
      assertThat(captor.getValue())
          .extracting(FeedDocument::getId)
          .containsExactly(id1.toString(), id2.toString());
    }

    @Test
    @DisplayName("청크 처리 후 영속성 컨텍스트를 비운다")
    void 청크_처리_후_영속성_컨텍스트를_비운다() {
      // given
      Chunk<Feed> chunk = new Chunk<>(List.of(feedWith(UUID.randomUUID(), "피드1")));

      // when
      writer.write(chunk);

      // then
      verify(entityManager).clear();
    }

    @Test
    @DisplayName("빈 청크는 저장을 호출하지 않는다")
    void 빈_청크는_저장을_호출하지_않는다() {
      // given
      Chunk<Feed> chunk = new Chunk<>(List.of());

      // when
      writer.write(chunk);

      // then
      verifyNoInteractions(feedSearchRepository);
      verifyNoInteractions(entityManager);
    }
  }
}
