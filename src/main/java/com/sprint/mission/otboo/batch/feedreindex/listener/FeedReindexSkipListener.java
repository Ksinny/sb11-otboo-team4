package com.sprint.mission.otboo.batch.feedreindex.listener;

import com.sprint.mission.otboo.batch.feedreindex.support.VersionConflicts;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FeedReindexSkipListener implements SkipListener<Feed, Feed> {

  private static final String SKIP_MARKER = "FEED_REINDEX_SKIPPED";


  @Override
  public void onSkipInWrite(Feed item, Throwable t) {
    if (VersionConflicts.isVersionConflict(t)) {
      // 더 최신 문서가 이미 색인돼 ES가 거부한 것이다.
      log.debug("피드 재색인 버전 충돌로 건너뜀: feedId={}", item.getId());
      return;
    }
    log.error("{} feedId={}", SKIP_MARKER, item.getId(), t);
  }
}
