package com.sprint.mission.otboo.domain.social.follow.repository.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.repository.FollowRepository;
import com.sprint.mission.otboo.global.config.JpaConfig;
import com.sprint.mission.otboo.global.config.QuerydslConfig;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
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
  }
}