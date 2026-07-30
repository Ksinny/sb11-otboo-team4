package com.sprint.mission.otboo.domain.social.follow.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import java.util.Optional;
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
@DisplayName("FollowRepository")
class FollowRepositoryTest {

  @Autowired
  private FollowRepository followRepository;

  @Autowired
  private TestEntityManager testEntityManager;

  @Nested
  @DisplayName("existsByFollowerIdAndFolloweeId")
  class ExistsByFollowerIdAndFolloweeId {

    @Test
    @DisplayName("팔로우가 존재하면 true를 반환한다")
    void 팔로우가_존재하면_true를_반환한다() {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();
      followRepository.save(Follow.create(followerId, followeeId));
      testEntityManager.flush();
      testEntityManager.clear();

      // when
      boolean result = followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("팔로우가 없으면 false를 반환한다")
    void 팔로우가_없으면_false를_반환한다() {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();

      // when
      boolean result = followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);

      // then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("findByFollowerIdAndFolloweeId")
  class FindByFollowerIdAndFolloweeId {

    @Test
    @DisplayName("팔로우가 존재하면 해당 Follow를 반환한다")
    void 팔로우가_존재하면_해당_Follow를_반환한다() {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();
      followRepository.save(Follow.create(followerId, followeeId));
      testEntityManager.flush();
      testEntityManager.clear();

      // when
      Optional<Follow> result =
          followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId);

      // then
      assertThat(result).isPresent();
      assertThat(result.get().getFollowerId()).isEqualTo(followerId);
      assertThat(result.get().getFolloweeId()).isEqualTo(followeeId);
    }

    @Test
    @DisplayName("팔로우가 없으면 빈 Optional을 반환한다")
    void 팔로우가_없으면_빈_Optional을_반환한다() {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();

      // when
      Optional<Follow> result =
          followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId);

      // then
      assertThat(result).isEmpty();
    }
  }
}