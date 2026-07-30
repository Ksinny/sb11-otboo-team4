package com.sprint.mission.otboo.domain.social.follow.service;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.entity.Follow;
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

    if (followerId.equals(followeeId)) {
      throw SelfFollowNotAllowedException.of(followerId);
    }
    
    Follow saved = followRepository.save(Follow.create(followerId, followeeId));

    UserSummary follower = userSummaryQueryRepository.findByUserId(followerId);
    UserSummary followee = userSummaryQueryRepository.findByUserId(followeeId);

    log.info("팔로우 생성 완료: followId={}, followerId={}, followeeId={}",
        saved.getId(), followerId, followeeId);

    return followMapper.toDto(saved, follower, followee);
  }
}