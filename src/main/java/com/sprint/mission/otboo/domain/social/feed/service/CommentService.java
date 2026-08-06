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
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final FeedRepository feedRepository;
  private final CommentMapper commentMapper;
  private final UserSummaryQueryRepository userSummaryQueryRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public CommentDto create(UUID feedId, CommentCreateRequest request, UUID currentUserId) {
    validateCreateRequest(feedId, request.authorId(), currentUserId);

    Comment comment = commentRepository.save(
        Comment.create(feedId, request.authorId(), request.content()));
    feedRepository.incrementCommentCount(feedId);
    log.info("피드 댓글 등록 완료: feedId={}, commentId={}", feedId, comment.getId());

    UserSummary author = userSummaryQueryRepository.findByUserId(comment.getAuthorId());
    publishCommentNotification(feedId, author.name(), comment.getContent());

    return commentMapper.toDto(comment, author);
  }

  private void validateCreateRequest(UUID feedId, UUID authorId, UUID currentUserId) {
    validateAuthorMatchesCurrentUser(authorId, currentUserId);
    validateFeedExists(feedId);
  }

  private void validateAuthorMatchesCurrentUser(UUID authorId, UUID currentUserId) {
    if (!authorId.equals(currentUserId)) {
      throw FeedForbiddenException.authorMismatch(currentUserId, authorId);
    }
  }

  private void validateFeedExists(UUID feedId) {
    if (!feedRepository.existsByIdAndSoftDeletable_DeletedAtIsNull(feedId)) {
      throw FeedNotFoundException.withId(feedId);
    }
  }

  private void publishCommentNotification(UUID feedId, String authorName, String content) {
    UUID feedAuthorId = feedRepository.findAuthorId(feedId)
        .orElseThrow(() -> FeedNotFoundException.withId(feedId));
    String title = authorName + "님이 댓글을 달았어요.";
    eventPublisher.publishEvent(
        new NotificationRequestedEvent(Set.of(feedAuthorId), title, content,
            NotificationLevel.INFO));
  }
}