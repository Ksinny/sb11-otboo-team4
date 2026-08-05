package com.sprint.mission.otboo.domain.social.feed.service;

import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.feed.dto.CommentCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.CommentDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Comment;
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
    Comment comment = commentRepository.save(
        Comment.create(request.feedId(), request.authorId(), request.content()));
    log.info("피드 댓글 등록 완료: feedId={}, commentId={}", request.feedId(), comment.getId());
    return commentMapper.toDto(comment, null);
  }
}