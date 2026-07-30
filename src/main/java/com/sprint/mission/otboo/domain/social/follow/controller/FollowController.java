package com.sprint.mission.otboo.domain.social.follow.controller;

import com.sprint.mission.otboo.domain.social.follow.controller.api.FollowApi;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowCreateRequest;
import com.sprint.mission.otboo.domain.social.follow.dto.FollowDto;
import com.sprint.mission.otboo.domain.social.follow.service.FollowService;
import com.sprint.mission.otboo.global.security.jwt.filter.CurrentUser;
import com.sprint.mission.otboo.global.security.jwt.filter.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController implements FollowApi {

  private final FollowService followService;

  @PostMapping
  @Override
  public ResponseEntity<FollowDto> createFollow(
      @Valid @RequestBody FollowCreateRequest request,
      @CurrentUser UserPrincipal principal) {
    return null; // TODO(green)
  }
}