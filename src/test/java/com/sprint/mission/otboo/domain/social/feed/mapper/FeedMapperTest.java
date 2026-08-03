package com.sprint.mission.otboo.domain.social.feed.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.PrecipitationType;
import com.sprint.mission.otboo.domain.weathernotification.weather.entity.enums.SkyStatus;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FeedMapper")
class FeedMapperTest {

  static final FixtureMonkey fm = FixtureMonkey.builder()
      .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
      .build();

  FeedMapper feedMapper = new FeedMapper();

  @Nested
  @DisplayName("toDto 변환")
  class ToDto {

    @Test
    @DisplayName("Feed 엔티티를 FeedDto로 변환하고 likedByMe를 전달값으로 채운다")
    void Feed_엔티티를_FeedDto로_변환하고_likedByMe를_전달값으로_채운다() {
      UserSummary author = new UserSummary(UUID.randomUUID(), "테스터", null);

      // given
      Feed feed = fm.giveMeBuilder(Feed.class)
          .set("content", "오늘의 착장")
          .set("likeCount", 0L)
          .set("commentCount", 0)
          .set("skyStatus", SkyStatus.CLEAR)
          .set("precipitationType", PrecipitationType.NONE)
          .set("precipitationAmount", 0.0)
          .set("precipitationProbability", 0.0)
          .set("temperatureCurrent", 28.0)
          .set("temperatureCompared", 2.0)
          .set("temperatureMin", 16.0)
          .set("temperatureMax", 31.0)
          .sample();

      // when
      FeedDto result = feedMapper.toDto(feed, author, false);

      // then
      assertThat(result.content()).isEqualTo("오늘의 착장");
      assertThat(result.likeCount()).isZero();
      assertThat(result.commentCount()).isZero();
      assertThat(result.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("Feed 엔티티와 UserSummary를 받아 FeedDto의 author 필드를 올바르게 채운다")
    void Feed_엔티티와_UserSummary를_받아_FeedDto의_author_필드를_올바르게_채운다() {
      // given
      Feed feed = fm.giveMeBuilder(Feed.class)
          .set("content", "오늘의 착장")
          .set("likeCount", 0L)
          .set("commentCount", 0)
          .set("skyStatus", SkyStatus.CLEAR)
          .set("precipitationType", PrecipitationType.NONE)
          .set("precipitationAmount", 0.0)
          .set("precipitationProbability", 0.0)
          .set("temperatureCurrent", 28.0)
          .set("temperatureCompared", 2.0)
          .set("temperatureMin", 16.0)
          .set("temperatureMax", 31.0)
          .sample();

      UserSummary mockAuthor = new UserSummary(UUID.randomUUID(), "테스트유저", "profile.png");

      // when
      FeedDto result = feedMapper.toDto(feed, mockAuthor, false);

      // then
      assertThat(result.author()).isNotNull();
      assertThat(result.author().userId()).isEqualTo(mockAuthor.userId());
      assertThat(result.author().name()).isEqualTo("테스트유저");
      assertThat(result.author().profileImageUrl()).isEqualTo("profile.png");
    }

    @Test
    @DisplayName("Feed flat 컬럼을 WeatherSummaryDto로 조립한다")
    void Feed_flat_컬럼을_WeatherSummaryDto로_조립한다() {
      // given
      UserSummary author = new UserSummary(UUID.randomUUID(), "테스터", null);
      Feed feed = fm.giveMeBuilder(Feed.class)
          .set("weatherId", UUID.randomUUID())
          .set("skyStatus", SkyStatus.CLEAR)
          .set("precipitationType", PrecipitationType.NONE)
          .set("precipitationAmount", 0.0)
          .set("precipitationProbability", 0.0)
          .set("temperatureCurrent", 28.0)
          .set("temperatureCompared", 2.0)
          .set("temperatureMin", 16.0)
          .set("temperatureMax", 31.0)
          .sample();

      // when
      FeedDto result = feedMapper.toDto(feed, author, false);

      // then
      assertThat(result.weather()).isNotNull();
      assertThat(result.weather().skyStatus()).isEqualTo(SkyStatus.CLEAR);
      assertThat(result.weather().precipitation().type()).isEqualTo(PrecipitationType.NONE);
      assertThat(result.weather().temperature().current()).isEqualTo(28.0);
      assertThat(result.weather().temperature().min()).isEqualTo(16.0);
      assertThat(result.weather().temperature().max()).isEqualTo(31.0);
    }

    @Test
    @DisplayName("skyStatus가 null이면 weather를 null로 반환한다")
    void skyStatus가_null이면_weather를_null로_반환한다() {
      // given
      UserSummary author = new UserSummary(UUID.randomUUID(), "테스터", null);
      Feed feed = fm.giveMeBuilder(Feed.class)
          .setNull("skyStatus")
          .sample();

      // when
      FeedDto result = feedMapper.toDto(feed, author, false);

      // then
      assertThat(result.weather()).isNull();
    }
  }
}