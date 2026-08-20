package com.sprint.mission.otboo.batch.feedreindex.writer;

import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.FeedSearchRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class FeedReindexWriter implements ItemWriter<Feed> {

  private final FeedSearchRepository feedSearchRepository;
  private final EntityManager entityManager;

  @Override
  public void write(Chunk<? extends Feed> chunk) {
  }
}
