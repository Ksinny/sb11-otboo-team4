package com.sprint.mission.otboo.domain.social.follow.repository.querydsl;

import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowingListParams;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;

public interface FollowCustomRepository {

  CursorPageResponse<FollowDto> findFollowings(FollowingListParams params);
}