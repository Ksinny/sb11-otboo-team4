package com.sprint.mission.otboo.batch.feedreindex.reader;

import com.sprint.mission.otboo.batch.feedreindex.config.FeedReindexProperties;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.stereotype.Component;

@Slf4j
@StepScope
@RequiredArgsConstructor
@Component
public class FeedReindexReader implements ItemReader<Feed> {

  private final FeedRepository feedRepository;
  private final FeedReindexProperties properties;

  @Override
  public Feed read() {
    return null;
  }
}
