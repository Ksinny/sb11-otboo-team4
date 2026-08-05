package com.sprint.mission.otboo.domain.social.feed.service;

import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.feed.dto.CommentCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.CommentDto;
import com.sprint.mission.otboo.domain.social.feed.mapper.CommentMapper;
import com.sprint.mission.otboo.domain.social.feed.repository.CommentRepository;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final FeedRepository feedRepository;
  private final CommentMapper commentMapper;
  private final UserSummaryQueryRepository userSummaryQueryRepository;

  @Transactional
  public CommentDto create(CommentCreateRequest request, UUID currentUserId) {
    return null;
  }
}