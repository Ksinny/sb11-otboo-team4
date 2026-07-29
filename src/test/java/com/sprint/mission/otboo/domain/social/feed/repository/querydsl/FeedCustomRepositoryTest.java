package com.sprint.mission.otboo.domain.social.feed.repository.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSortBy;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import com.sprint.mission.otboo.global.dto.SortDirection;
import java.time.Instant;
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

  private void setLikeCount(UUID feedId, long count) {
    testEntityManager.getEntityManager()
        .createNativeQuery("update feeds set like_count = :count where id = :id")
        .setParameter("count", count)
        .setParameter("id", feedId)
        .executeUpdate();
  }

  private void setCreatedAt(UUID feedId, Instant createdAt) {
    testEntityManager.getEntityManager()
        .createNativeQuery("update feeds set created_at = :createdAt where id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", feedId)
        .executeUpdate();
  }

  @Nested
  @DisplayName("findFeeds")
  class FindFeeds {

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

    @Test
    @DisplayName("커서 이후의 피드만 조회한다")
    void returnsFeedsAfterCursor_whenCursorGiven() {
      // given
      Feed first = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "첫번째"));
      Feed second = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "두번째"));
      Feed third = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "세번째"));
      testEntityManager.flush();
      testEntityManager.clear();

      // DESC 정렬 시 순서: third → second → first
      FeedListParams params = new FeedListParams(
          third.getCreatedAt().toString(), third.getId(), 10,
          FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
          null, null
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result)
          .extracting(Feed::getContent)
          .containsExactly("두번째", "첫번째");
    }

    @Test
    @DisplayName("authorIdEqual이 주어지면 해당 작성자의 피드만 조회한다")
    void returnsOnlyAuthorFeeds_whenAuthorIdEqualGiven() {
      // given
      UUID targetAuthor = UUID.randomUUID();
      UUID otherAuthor = UUID.randomUUID();
      feedRepository.save(Feed.create(targetAuthor, UUID.randomUUID(), "대상 작성자 글"));
      feedRepository.save(Feed.create(otherAuthor, UUID.randomUUID(), "다른 작성자 글"));
      testEntityManager.flush();
      testEntityManager.clear();

      FeedListParams params = new FeedListParams(
          null, null, 10,
          FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
          null, targetAuthor
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result)
          .extracting(Feed::getAuthorId)
          .containsExactly(targetAuthor);
    }

    @Test
    @DisplayName("keywordLike가 주어지면 content에 해당 키워드를 포함한 피드만 조회한다")
    void returnsOnlyMatchingFeeds_whenKeywordLikeGiven() {
      // given
      feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "맑은 날 OOTD"));
      feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "비 오는 날 OOTD"));
      feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "흐린 날 패션"));
      testEntityManager.flush();
      testEntityManager.clear();

      FeedListParams params = new FeedListParams(
          null, null, 10,
          FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
          "OOTD", null
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result)
          .extracting(Feed::getContent)
          .containsExactlyInAnyOrder("맑은 날 OOTD", "비 오는 날 OOTD");
    }

    @Test
    @DisplayName("sortBy가 likeCount면 좋아요 수 내림차순으로 조회한다")
    void returnsSortedByLikeCountDesc_whenSortByLikeCount() {
      // given
      Feed low = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "좋아요 적음"));
      Feed high = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "좋아요 많음"));
      Feed mid = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "좋아요 중간"));
      testEntityManager.flush();

      setLikeCount(low.getId(), 1L);
      setLikeCount(high.getId(), 100L);
      setLikeCount(mid.getId(), 50L);
      testEntityManager.clear();

      FeedListParams params = new FeedListParams(
          null, null, 10,
          FeedSortBy.LIKE_COUNT, SortDirection.DESCENDING,
          null, null
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result)
          .extracting(Feed::getContent)
          .containsExactly("좋아요 많음", "좋아요 중간", "좋아요 적음");
    }

    @Test
    @DisplayName("sortDirection이 ASCENDING이면 오래된 순으로 조회한다")
    void returnsSortedByCreatedAtAsc_whenDirectionAscending() {
      // given
      feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "첫번째"));
      feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "두번째"));
      feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "세번째"));
      testEntityManager.flush();
      testEntityManager.clear();

      FeedListParams params = new FeedListParams(
          null, null, 10,
          FeedSortBy.CREATED_AT, SortDirection.ASCENDING,
          null, null
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result)
          .extracting(Feed::getContent)
          .containsExactly("첫번째", "두번째", "세번째");
    }

    @Test
    @DisplayName("likeCount 정렬에서 커서 이후의 피드만 조회한다")
    void returnsFeedsAfterCursor_whenSortByLikeCountWithCursor() {
      // given
      Feed a = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "A"));
      Feed b = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "B"));
      Feed c = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "C"));
      testEntityManager.flush();

      setLikeCount(a.getId(), 100L);
      setLikeCount(b.getId(), 50L);
      setLikeCount(c.getId(), 10L);
      testEntityManager.clear();

      // likeCount DESC 순서: A(100) → B(50) → C(10)
      FeedListParams params = new FeedListParams(
          "50", b.getId(), 10,
          FeedSortBy.LIKE_COUNT, SortDirection.DESCENDING,
          null, null
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result)
          .extracting(Feed::getContent)
          .containsExactly("C");
    }

    @Test
    @DisplayName("createdAt 오름차순에서 커서 이후의 피드만 조회한다")
    void returnsFeedsAfterCursor_whenCreatedAtAscWithCursor() {
      // given
      Feed first = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "첫번째"));
      Feed second = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "두번째"));
      Feed third = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "세번째"));
      testEntityManager.flush();
      testEntityManager.clear();

      // ASC 순서: 첫번째 → 두번째 → 세번째
      FeedListParams params = new FeedListParams(
          first.getCreatedAt().toString(), first.getId(), 10,
          FeedSortBy.CREATED_AT, SortDirection.ASCENDING,
          null, null
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result)
          .extracting(Feed::getContent)
          .containsExactly("두번째", "세번째");
    }

    @Test
    @DisplayName("createdAt이 같으면 id 역순으로 tie-break하여 조회한다")
    void breaksTieById_whenCreatedAtEqual() {
      // given
      Instant sameTime = Instant.parse("2026-07-28T00:00:00Z");
      Feed a = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "A"));
      Feed b = feedRepository.save(Feed.create(UUID.randomUUID(), UUID.randomUUID(), "B"));
      testEntityManager.flush();

      setCreatedAt(a.getId(), sameTime);
      setCreatedAt(b.getId(), sameTime);
      testEntityManager.clear();

      // 실제 부여된 id로 기대 순서 계산 (DESC tie-break: 큰 id 먼저)
      UUID largerId = a.getId().compareTo(b.getId()) > 0 ? a.getId() : b.getId();
      UUID smallerId = a.getId().compareTo(b.getId()) > 0 ? b.getId() : a.getId();

      FeedListParams params = new FeedListParams(
          null, null, 10,
          FeedSortBy.CREATED_AT, SortDirection.DESCENDING,
          null, null
      );

      // when
      List<Feed> result = feedRepository.findFeeds(params);

      // then
      assertThat(result)
          .extracting(Feed::getId)
          .containsExactly(largerId, smallerId);
    }
  }
}