package com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.impl;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSearchResult;
import com.sprint.mission.otboo.domain.social.feed.repository.elasticsearch.FeedSearchCustomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

@RequiredArgsConstructor
public class FeedSearchCustomRepositoryImpl implements FeedSearchCustomRepository {

  private final ElasticsearchOperations operations;

  @Override
  public FeedSearchResult search(FeedListParams params) {
    return new FeedSearchResult(List.of(), 0L, null, null, false);
  }
}
