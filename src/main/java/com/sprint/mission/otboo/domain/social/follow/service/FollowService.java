package com.sprint.mission.otboo.domain.social.follow.service;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
import com.sprint.mission.otboo.domain.social.follow.exception.FollowForbiddenException;
import com.sprint.mission.otboo.domain.social.follow.exception.SelfFollowNotAllowedException;
import com.sprint.mission.otboo.domain.social.follow.mapper.FollowMapper;
import com.sprint.mission.otboo.domain.social.follow.repository.FollowRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class FollowService {

  private final FollowRepository followRepository;
  private final FollowMapper followMapper;
  private final UserSummaryQueryRepository userSummaryQueryRepository;

  @Transactional
  public FollowDto create(FollowCreateRequest request, UUID currentUserId) {
    UUID followerId = request.followerId();
    UUID followeeId = request.followeeId();

    validateFollowerMatchesCurrentUser(followerId, currentUserId);
    validateNotSelfFollow(followerId, followeeId);

    Follow follow = findOrCreateFollow(followerId, followeeId);
    return toDto(follow);
  }

  private void validateFollowerMatchesCurrentUser(UUID followerId, UUID currentUserId) {
    if (!followerId.equals(currentUserId)) {
      throw FollowForbiddenException.followerMismatch(currentUserId, followerId);
    }
  }

  private void validateNotSelfFollow(UUID followerId, UUID followeeId) {
    if (followerId.equals(followeeId)) {
      throw SelfFollowNotAllowedException.of(followerId);
    }
  }

  private Follow findOrCreateFollow(UUID followerId, UUID followeeId) {
    if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
      return followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
          .orElseThrow();
    }
    Follow saved = followRepository.save(Follow.create(followerId, followeeId));

    log.info("팔로우 생성 완료: followId={}, followerId={}, followeeId={}",
        saved.getId(), followerId, followeeId);
    return saved;
  }

  private FollowDto toDto(Follow follow) {
    UserSummary follower = userSummaryQueryRepository.findByUserId(follow.getFollowerId());
    UserSummary followee = userSummaryQueryRepository.findByUserId(follow.getFolloweeId());
    return followMapper.toDto(follow, follower, followee);
  }
}