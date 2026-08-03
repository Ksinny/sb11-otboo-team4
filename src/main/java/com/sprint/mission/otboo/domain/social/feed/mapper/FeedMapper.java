package com.sprint.mission.otboo.domain.social.feed.mapper;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.weathernotification.weather.dto.PrecipitationDto;
import com.sprint.mission.otboo.domain.weathernotification.weather.dto.TemperatureDto;
import com.sprint.mission.otboo.domain.weathernotification.weather.dto.WeatherSummaryDto;
import org.springframework.stereotype.Component;

@Component
public class FeedMapper {

  public FeedDto toDto(Feed feed, UserSummary author, boolean likedByMe) {
    return new FeedDto(
        feed.getId(),
        feed.getCreatedAt(),
        feed.getUpdatedAt(),
        author,
        toWeatherSummaryDto(feed),
        feed.getOotds(),
        feed.getContent(),
        feed.getLikeCount(),
        feed.getCommentCount(),
        likedByMe
    );
  }

  private WeatherSummaryDto toWeatherSummaryDto(Feed feed) {
    if (feed.getSkyStatus() == null) {
      return null;
    }
    return new WeatherSummaryDto(
        feed.getWeatherId(),
        feed.getSkyStatus(),
        new PrecipitationDto(
            feed.getPrecipitationType(),
            feed.getPrecipitationAmount() != null ? feed.getPrecipitationAmount() : 0.0,
            feed.getPrecipitationProbability() != null ? feed.getPrecipitationProbability() : 0.0
        ),
        new TemperatureDto(
            feed.getTemperatureCurrent() != null ? feed.getTemperatureCurrent() : 0.0,
            feed.getTemperatureCompared() != null ? feed.getTemperatureCompared() : 0.0,
            feed.getTemperatureMin() != null ? feed.getTemperatureMin() : 0.0,
            feed.getTemperatureMax() != null ? feed.getTemperatureMax() : 0.0
        )
    );
  }
}