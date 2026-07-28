package com.sprint.mission.otboo.domain.social.feed.repository.querydsl;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import java.util.List;

public interface FeedCustomRepository {

  List<Feed> findFeeds(FeedListParams params);
  
}