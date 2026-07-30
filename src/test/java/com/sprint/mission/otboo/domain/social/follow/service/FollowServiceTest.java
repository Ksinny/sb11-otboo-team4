package com.sprint.mission.otboo.domain.social.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.mapper.FollowMapper;
import com.sprint.mission.otboo.domain.social.follow.repository.FollowRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("FollowService")
class FollowServiceTest {

  private static final FixtureMonkey fm = FixtureMonkey.builder()
      .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
      .plugin(new JakartaValidationPlugin())
      .build();

  @InjectMocks
  private FollowService followService;

  @Mock
  private FollowRepository followRepository;

  @Mock
  private FollowMapper followMapper;

  @Mock
  private UserSummaryQueryRepository userSummaryQueryRepository;

  @Nested
  @DisplayName("팔로우 등록")
  class CreateFollow {

    @Test
    @DisplayName("정상 요청이면 Follow를 저장하고 FollowDto를 반환한다")
    void 정상_요청이면_Follow를_저장하고_FollowDto를_반환한다() {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();
      FollowCreateRequest request = fm.giveMeBuilder(FollowCreateRequest.class)
          .set("followerId", followerId)
          .set("followeeId", followeeId)
          .sample();

      given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
          .willReturn(false);
      given(followRepository.save(any(Follow.class)))
          .willAnswer(inv -> inv.getArgument(0));

      UserSummary followerSummary = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followerId).sample();
      UserSummary followeeSummary = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followeeId).sample();
      given(userSummaryQueryRepository.findByUserId(followerId)).willReturn(followerSummary);
      given(userSummaryQueryRepository.findByUserId(followeeId)).willReturn(followeeSummary);

      FollowDto expected = fm.giveMeOne(FollowDto.class);
      given(followMapper.toDto(any(Follow.class), eq(followerSummary), eq(followeeSummary)))
          .willReturn(expected);

      // when
      FollowDto result = followService.create(request, followerId);

      // then
      assertThat(result).isEqualTo(expected);
    }
  }
}