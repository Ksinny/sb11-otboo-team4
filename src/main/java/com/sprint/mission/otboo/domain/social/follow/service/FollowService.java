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
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class FollowService {

  private static final String UQ_FOLLOWS = "uq_follows_follower_id_followee_id";

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

  private Follow findOrCreateFollow(UUID followerId, UUID followeeId) {
    if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
      return findExistingFollow(followerId, followeeId);
    }
    try {
      Follow saved = followRepository.save(Follow.create(followerId, followeeId));
      log.info("팔로우 생성 완료: followId={}, followerId={}, followeeId={}",
          saved.getId(), followerId, followeeId);
      return saved;
    } catch (DataIntegrityViolationException e) {
      if (isUniqueViolation(e)) {
        return findExistingFollow(followerId, followeeId);
      }
      throw e;
    }
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


  private Follow findExistingFollow(UUID followerId, UUID followeeId) {
    return followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
        .orElseThrow();
  }

  private boolean isUniqueViolation(DataIntegrityViolationException e) {
    return e.getCause() instanceof ConstraintViolationException cve
        && UQ_FOLLOWS.equalsIgnoreCase(cve.getConstraintName());
  }

  private FollowDto toDto(Follow follow) {
    UserSummary follower = userSummaryQueryRepository.findByUserId(follow.getFollowerId());
    UserSummary followee = userSummaryQueryRepository.findByUserId(follow.getFolloweeId());
    return followMapper.toDto(follow, follower, followee);
  }
}