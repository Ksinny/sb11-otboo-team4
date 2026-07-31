package com.sprint.mission.otboo.domain.social.follow.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowSummaryDto;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("FollowMapper")
class FollowMapperTest {

  private static final FixtureMonkey fm = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  private final FollowMapper followMapper = new FollowMapper();

  @Nested
  @DisplayName("toDto")
  class ToDto {

    @Test
    @DisplayName("Follow와 UserSummary들을 FollowDto로 변환한다")
    void Follow와_UserSummary들을_FollowDto로_변환한다() {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();
      Follow follow = Follow.create(followerId, followeeId);
      UserSummary follower = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followerId)
          .sample();
      UserSummary followee = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followeeId)
          .sample();

      // when
      FollowDto result = followMapper.toDto(follow, follower, followee);

      // then
      assertThat(result.id()).isEqualTo(follow.getId());
      assertThat(result.follower()).isEqualTo(follower);
      assertThat(result.followee()).isEqualTo(followee);
    }
  }

  @Nested
  @DisplayName("toSummaryDto")
  class ToSummaryDto {

    @Test
    @DisplayName("전달받은 정보를 FollowSummaryDto로 변환한다")
    void 전달받은_정보를_FollowSummaryDto로_변환한다() {
      // given
      UUID followeeId = UUID.randomUUID();
      long followerCount = 10L;
      long followingCount = 5L;
      boolean followedByMe = true;
      UUID followedByMeId = UUID.randomUUID();
      boolean followingMe = false;

      // when
      FollowSummaryDto result = followMapper.toSummaryDto(
          followeeId, followerCount, followingCount, followedByMe, followedByMeId, followingMe);

      // then
      assertThat(result.followeeId()).isEqualTo(followeeId);
      assertThat(result.followerCount()).isEqualTo(followerCount);
      assertThat(result.followingCount()).isEqualTo(followingCount);
      assertThat(result.followedByMe()).isEqualTo(followedByMe);
      assertThat(result.followedByMeId()).isEqualTo(followedByMeId);
      assertThat(result.followingMe()).isEqualTo(followingMe);
    }
  }
}