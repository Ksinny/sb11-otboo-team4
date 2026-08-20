package com.sprint.mission.otboo.batch.feedreindex.reader;

import com.sprint.mission.otboo.batch.feedreindex.config.FeedReindexProperties;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@Component
public class FeedIncrementalReindexReader implements ItemReader<Feed> {

  private final FeedRepository feedRepository;
  private final FeedReindexProperties properties;
  private final Instant since;

  public FeedIncrementalReindexReader(FeedRepository feedRepository,
      FeedReindexProperties properties,
      @Value("#{jobParameters['since']}") Long sinceEpochMilli) {
    this.feedRepository = feedRepository;
    this.properties = properties;
    this.since = Instant.ofEpochMilli(sinceEpochMilli);
  }

  @Override
  public Feed read() {
    return null;
  }
}
