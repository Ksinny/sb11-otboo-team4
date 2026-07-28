package com.sprint.mission.otboo.domain.social.feed.repository.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSortBy;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaConfig.class, QuerydslConfig.class})
@DisplayName("FeedCustomRepository")
class FeedCustomRepositoryTest {

  @Autowired
  private FeedRepository feedRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  @Nested
  @DisplayName("findFeeds - 첫 페이지 조회")
  class FindFeedsFirstPage {

    @Test
    @DisplayName("커서가 없으면 limit + 1개까지 조회한다")
    void returnsLimitPlusOne_whenNoCursor() {
      // given
      for (int i = 0; i < 3; i++) {
        feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "내용" + i));
      }
      testEntityManager.flush();
      testEntityManager.clear();

      FeedListParams params = new FeedListParams(
          null, null, 2,
          FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
          null, null
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result).hasSize(3); // limit(2) + 1
    }
  }
}