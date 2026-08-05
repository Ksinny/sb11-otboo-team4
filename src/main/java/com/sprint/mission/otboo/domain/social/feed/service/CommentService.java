package com.sprint.mission.otboo.domain.social.feed.service;

import com.sprint.mission.otboo.domain.social.common.dto.UserSummary;
import com.sprint.mission.otboo.domain.social.common.repository.querydsl.UserSummaryQueryRepository;
import com.sprint.mission.otboo.domain.social.feed.dto.CommentCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.CommentDto;
import com.sprint.mission.otboo.domain.social.feed.entity.Comment;
import com.sprint.mission.otboo.domain.social.feed.exception.FeedForbiddenException;
import com.sprint.mission.otboo.domain.social.feed.exception.FeedNotFoundException;
import com.sprint.mission.otboo.domain.social.feed.mapper.CommentMapper;
import com.sprint.mission.otboo.domain.social.feed.repository.CommentRepository;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import com.sprint.mission.otboo.global.event.NotificationLevel;
import com.sprint.mission.otboo.global.event.NotificationRequestedEvent;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public CommentDto create(CommentCreateRequest request, UUID currentUserId) {
    if (!request.authorId().equals(currentUserId)) {
      throw FeedForbiddenException.authorMismatch(currentUserId, request.authorId());
    }
    if (!feedRepository.existsByIdAndSoftDeletable_DeletedAtIsNull(request.feedId())) {
      throw FeedNotFoundException.withId(request.feedId());
    }
    Comment comment = commentRepository.save(
        Comment.create(request.feedId(), request.authorId(), request.content()));
    feedRepository.incrementCommentCount(request.feedId());
    log.info("피드 댓글 등록 완료: feedId={}, commentId={}", request.feedId(), comment.getId());

    UserSummary author = userSummaryQueryRepository.findByUserId(comment.getAuthorId());

    UUID feedAuthorId = feedRepository.findAuthorId(comment.getFeedId())
        .orElseThrow(() -> FeedNotFoundException.withId(comment.getFeedId()));
    eventPublisher.publishEvent(new NotificationRequestedEvent(
        Set.of(feedAuthorId), author.name() + "님이 댓글을 달았어요.",
        comment.getContent(), NotificationLevel.INFO));

    return commentMapper.toDto(comment, author);
  }
}