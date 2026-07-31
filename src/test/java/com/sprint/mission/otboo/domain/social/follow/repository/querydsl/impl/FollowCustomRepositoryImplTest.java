package com.sprint.mission.otboo.domain.social.follow.repository.querydsl.impl;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.authuser.user.entity.User;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.repository.FollowRepository;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
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
@DisplayName("FollowCustomRepository")
class FollowCustomRepositoryTest {

  @Autowired
  private FollowRepository followRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  private void setCreatedAt(UUID followId, Instant createdAt) {
    testEntityManager.getEntityManager()
        .createNativeQuery("update follows set created_at = :createdAt where id = :id")
        .setParameter("createdAt", createdAt)
        .setParameter("id", followId)
        .executeUpdate();
  }

  @Nested
  @DisplayName("findFollowings")
  class FindFollowings {

    @Test
    @DisplayName("특정 follower의 팔로잉만 limit + 1개까지 조회한다")
    void 특정_follower의_팔로잉만_limit_플러스_1개까지_조회한다() {
      // given
      UUID followerId = UUID.randomUUID();
      for (int i = 0; i < 3; i++) {
        followRepository.save(Follow.create(followerId, UUID.randomUUID()));
      }
      followRepository.save(Follow.create(UUID.randomUUID(), UUID.randomUUID())); // 다른 follower
      testEntityManager.flush();
      testEntityManager.clear();

      FollowingListParams params = new FollowingListParams(followerId, null, null, 2, null);

      // when
      CursorPageResponse<Follow> result = followRepository.findFollowings(params);

      // then
      assertThat(result.data()).hasSize(2);
      assertThat(result.hasNext()).isTrue();
      assertThat(result.totalCount()).isEqualTo(3);
      assertThat(result.data())
          .allSatisfy(f -> assertThat(f.getFollowerId()).isEqualTo(followerId));
    }

    @Test
    @DisplayName("커서 이후의 팔로잉만 조회한다")
    void 커서_이후의_팔로잉만_조회한다() {
      // given
      UUID followerId = UUID.randomUUID();
      Follow first = followRepository.save(Follow.create(followerId, UUID.randomUUID()));
      Follow second = followRepository.save(Follow.create(followerId, UUID.randomUUID()));
      Follow third = followRepository.save(Follow.create(followerId, UUID.randomUUID()));
      testEntityManager.flush();
      testEntityManager.clear();

      // createdAt DESC 순서: third → second → first, 커서 = third
      FollowingListParams params = new FollowingListParams(
          followerId, third.getCreatedAt().toString(), third.getId(), 10, null);

      // when
      CursorPageResponse<Follow> result = followRepository.findFollowings(params);

      // then
      assertThat(result.data())
          .extracting(Follow::getFolloweeId)
          .containsExactly(second.getFolloweeId(), first.getFolloweeId());
    }

    @Test
    @DisplayName("createdAt이 같으면 id 역순으로 tie-break하여 조회한다")
    void createdAt이_같으면_id_역순으로_tie_break하여_조회한다() {
      // given
      UUID followerId = UUID.randomUUID();
      Instant sameTime = Instant.parse("2026-07-28T00:00:00Z");
      Follow a = followRepository.save(Follow.create(followerId, UUID.randomUUID()));
      Follow b = followRepository.save(Follow.create(followerId, UUID.randomUUID()));
      testEntityManager.flush();

      setCreatedAt(a.getId(), sameTime);
      setCreatedAt(b.getId(), sameTime);
      testEntityManager.clear();

      FollowingListParams params = new FollowingListParams(followerId, null, null, 10, null);

      // when
      CursorPageResponse<Follow> result = followRepository.findFollowings(params);

      // then
      // 같은 createdAt이므로 id DESC로 tie-break되어 두 팔로우 모두 조회됨
      assertThat(result.data())
          .extracting(Follow::getId)
          .containsExactlyInAnyOrder(a.getId(), b.getId());
      // tie-break 검증
      assertThat(result.data().get(0).getId().toString())
          .isGreaterThan(result.data().get(1).getId().toString());
    }

    @Test
    @DisplayName("createdAt 동률에서 커서로 다음 페이지를 조회하면 나머지가 중복·누락 없이 조회된다")
    void createdAt_동률에서_커서로_다음_페이지를_조회하면_나머지가_중복_누락_없이_조회된다() {
      // given
      UUID followerId = UUID.randomUUID();
      Instant sameTime = Instant.parse("2026-07-28T00:00:00Z");
      Follow a = followRepository.save(Follow.create(followerId, UUID.randomUUID()));
      Follow b = followRepository.save(Follow.create(followerId, UUID.randomUUID()));
      testEntityManager.flush();

      setCreatedAt(a.getId(), sameTime);
      setCreatedAt(b.getId(), sameTime);
      testEntityManager.clear();

      FollowingListParams firstPage = new FollowingListParams(followerId, null, null, 1, null);

      // when: 첫 페이지 조회
      CursorPageResponse<Follow> first = followRepository.findFollowings(firstPage);

      // then
      assertThat(first.data()).hasSize(1);
      assertThat(first.hasNext()).isTrue();
      assertThat(first.nextCursor()).isNotNull();
      assertThat(first.nextIdAfter()).isNotNull();

      UUID firstId = first.data().get(0).getId();

      // when: 커서로 다음 페이지 조회
      FollowingListParams secondPage = new FollowingListParams(
          followerId, first.nextCursor(), first.nextIdAfter(), 1, null);
      CursorPageResponse<Follow> second = followRepository.findFollowings(secondPage);

      // then
      assertThat(second.data()).hasSize(1);
      UUID secondId = second.data().get(0).getId();
      assertThat(secondId).isNotEqualTo(firstId);
      assertThat(List.of(firstId, secondId)).containsExactlyInAnyOrder(a.getId(), b.getId());
    }

    @Test
    @DisplayName("nameLike가 주어지면 팔로위 이름에 해당 키워드를 포함한 팔로잉만 조회한다")
    void nameLike가_주어지면_팔로위_이름에_해당_키워드를_포함한_팔로잉만_조회한다() {
      // given
      UUID followerId = UUID.randomUUID();
      User woody = testEntityManager.persist(User.create("우디", "woody@otboo.io", "password"));
      User buzz = testEntityManager.persist(User.create("버즈", "buzz@otboo.io", "password"));
      User woodyFriend = testEntityManager.persist(User.create("우디친구", "wf@otboo.io", "password"));
      followRepository.save(Follow.create(followerId, woody.getId()));
      followRepository.save(Follow.create(followerId, buzz.getId()));
      followRepository.save(Follow.create(followerId, woodyFriend.getId()));
      testEntityManager.flush();
      testEntityManager.clear();

      FollowingListParams params = new FollowingListParams(followerId, null, null, 10, "우디");

      // when
      CursorPageResponse<Follow> result = followRepository.findFollowings(params);

      // then
      assertThat(result.data())
          .extracting(Follow::getFolloweeId)
          .containsExactlyInAnyOrder(woody.getId(), woodyFriend.getId());
      assertThat(result.totalCount()).isEqualTo(2);
    }
  }
}