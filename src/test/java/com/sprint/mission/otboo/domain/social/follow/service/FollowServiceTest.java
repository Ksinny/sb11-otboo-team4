package com.sprint.mission.otboo.domain.social.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.exception.FollowForbiddenException;
import com.sprint.mission.otboo.domain.social.follow.exception.SelfFollowNotAllowedException;
import com.sprint.mission.otboo.domain.social.follow.mapper.FollowMapper;
import com.sprint.mission.otboo.domain.social.follow.repository.FollowRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

      UserSummary followerSummary = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followerId).sample();
      UserSummary followeeSummary = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followeeId).sample();

      FollowDto expected = new FollowDto(UUID.randomUUID(), followeeSummary, followerSummary);

      given(followRepository.save(any(Follow.class)))
          .willAnswer(inv -> inv.getArgument(0));
      given(userSummaryQueryRepository.findByUserId(followerId)).willReturn(followerSummary);
      given(userSummaryQueryRepository.findByUserId(followeeId)).willReturn(followeeSummary);
      given(followMapper.toDto(any(Follow.class), eq(followerSummary), eq(followeeSummary)))
          .willReturn(expected);

      // when
      FollowDto result = followService.create(request, followerId);

      // then
      assertThat(result).isEqualTo(expected);

      ArgumentCaptor<Follow> followCaptor = ArgumentCaptor.forClass(Follow.class);
      verify(followRepository).save(followCaptor.capture());
      Follow savedFollow = followCaptor.getValue();
      assertThat(savedFollow.getFollowerId()).isEqualTo(followerId);
      assertThat(savedFollow.getFolloweeId()).isEqualTo(followeeId);

      verify(userSummaryQueryRepository).findByUserId(followerId);
      verify(userSummaryQueryRepository).findByUserId(followeeId);
    }

    @Test
    @DisplayName("자기 자신을 팔로우하면 SelfFollowNotAllowedException을 던진다")
    void 자기_자신을_팔로우하면_SelfFollowNotAllowedException을_던진다() {
      // given
      UUID userId = UUID.randomUUID();
      FollowCreateRequest request = fm.giveMeBuilder(FollowCreateRequest.class)
          .set("followerId", userId)
          .set("followeeId", userId)
          .sample();

      // when & then
      assertThatThrownBy(() -> followService.create(request, userId))
          .isInstanceOf(SelfFollowNotAllowedException.class);
    }


    @Test
    @DisplayName("요청자가 인증 사용자와 다르면 FollowForbiddenException을 던진다")
    void 요청자가_인증_사용자와_다르면_FollowForbiddenException을_던진다() {
      // given
      UUID currentUserId = UUID.randomUUID();
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();
      FollowCreateRequest request = fm.giveMeBuilder(FollowCreateRequest.class)
          .set("followerId", followerId)
          .set("followeeId", followeeId)
          .sample();

      // when & then
      assertThatThrownBy(() -> followService.create(request, currentUserId))
          .isInstanceOf(FollowForbiddenException.class);
    }

    @Test
    @DisplayName("이미 팔로우 중이면 저장하지 않고 기존 Follow로 FollowDto를 반환한다")
    void 이미_팔로우_중이면_저장하지_않고_기존_Follow로_FollowDto를_반환한다() {
      // given
      UUID followerId = UUID.randomUUID();
      UUID followeeId = UUID.randomUUID();
      FollowCreateRequest request = fm.giveMeBuilder(FollowCreateRequest.class)
          .set("followerId", followerId)
          .set("followeeId", followeeId)
          .sample();

      Follow existing = Follow.create(followerId, followeeId);
      UserSummary followerSummary = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followerId).sample();
      UserSummary followeeSummary = fm.giveMeBuilder(UserSummary.class)
          .set("userId", followeeId).sample();
      FollowDto expected = new FollowDto(existing.getId(), followeeSummary, followerSummary);

      given(followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId))
          .willReturn(true);
      given(followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId))
          .willReturn(Optional.of(existing));
      given(userSummaryQueryRepository.findByUserId(followerId)).willReturn(followerSummary);
      given(userSummaryQueryRepository.findByUserId(followeeId)).willReturn(followeeSummary);
      given(followMapper.toDto(eq(existing), eq(followerSummary), eq(followeeSummary)))
          .willReturn(expected);

      // when
      FollowDto result = followService.create(request, followerId);

      // then
      assertThat(result).isEqualTo(expected);
      verify(followRepository, never()).save(any(Follow.class));
    }
  }
}