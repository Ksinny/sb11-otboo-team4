package com.sprint.mission.otboo.batch.feedreindex.listener;

import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeedReindexSkipListener implements SkipListener<Feed, Feed> {

  @Override
  public void onSkipInWrite(Feed item, Throwable t) {
    // 어느 문서가 왜 실패했는지 남긴다. 없으면 Job 실패 로그만으로는
    // ES 장애인지 특정 문서 문제인지 구분할 수 없다.
    log.error("FEED_REINDEX_SKIPPED feedId={}", item.getId(), t);
  }
}
