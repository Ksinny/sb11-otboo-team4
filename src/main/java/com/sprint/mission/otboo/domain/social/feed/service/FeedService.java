package com.sprint.mission.otboo.domain.social.feed.service;

import com.sprint.mission.otboo.domain.social.feed.dto.FeedCreateRequest;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedDto;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedListParams;
import com.sprint.mission.otboo.domain.social.feed.dto.FeedSortBy;
import com.sprint.mission.otboo.domain.social.feed.entity.Feed;
import com.sprint.mission.otboo.domain.social.feed.exception.FeedForbiddenException;
import com.sprint.mission.otboo.domain.social.feed.mapper.FeedMapper;
import com.sprint.mission.otboo.domain.social.feed.repository.FeedRepository;
import com.sprint.mission.otboo.global.dto.CursorPageResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class FeedService {

  private final FeedRepository feedRepository;
  private final FeedMapper feedMapper;

  @Transactional
  public FeedDto create(FeedCreateRequest request, UUID currentUserId) {
    if (!request.authorId().equals(currentUserId)) {
      throw FeedForbiddenException.authorMismatch(currentUserId, request.authorId());
    }
    Feed feed = feedRepository.save(
        Feed.create(request.authorId(), request.weatherId(), request.content()));
    log.info("피드 등록 완료: feedId={}, authorId={}", feed.getId(), feed.getAuthorId());
    return feedMapper.toDto(feed, false);
  }

  public CursorPageResponse<FeedDto> getFeeds(FeedListParams params) {
    List<Feed> feeds = feedRepository.findFeeds(params);

    boolean hasNext = feeds.size() > params.limit();
    List<Feed> page = hasNext ? feeds.subList(0, params.limit()) : feeds;

    List<FeedDto> data = page.stream()
        .map(feed -> feedMapper.toDto(feed, false))
        .toList();

    String nextCursor = null;
    UUID nextIdAfter = null;
    if (hasNext && !page.isEmpty()) {
      Feed last = page.get(page.size() - 1);
      nextCursor = extractCursor(last, params.sortBy());
      nextIdAfter = last.getId();
    }

    log.info("피드 목록 조회 완료: 조회 건수={}, hasNext={}", data.size(), hasNext);

    return new CursorPageResponse<>(
        data, nextCursor, nextIdAfter, hasNext, data.size(),
        params.sortBy().param(), params.sortDirection());
  }

  private String extractCursor(Feed last, FeedSortBy sortBy) {
    return switch (sortBy) {
      case CREATED_AT -> last.getCreatedAt().toString();
      case LIKE_COUNT -> String.valueOf(last.getLikeCount());
    };
  }
}