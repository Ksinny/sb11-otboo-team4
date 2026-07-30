package com.sprint.mission.otboo.domain.social.follow.controller.api;

import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.global.security.jwt.filter.UserPrincipal;
import org.springframework.http.ResponseEntity;

public interface FollowApi {

  ResponseEntity<FollowDto> createFollow(FollowCreateRequest request, UserPrincipal principal);
}